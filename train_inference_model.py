# 패키지 임포트
from tensorflow.keras.callbacks import LearningRateScheduler, LambdaCallback
from tensorflow.keras.models import Model
from tensorflow.keras import backend as K
from tensorflow.keras.layers import Activation, Add, BatchNormalization, Conv2D, Dense, GlobalAveragePooling2D, Input
from tensorflow.keras.regularizers import l2
from pathlib import Path
import numpy as np
import pickle
import os


# 파라미터 준비
RN_EPOCHS = 100  # 학습 횟수
DN_FILTERS = 128  # 컨볼루션 레이어 커널 수(오리지널: 256))
DN_RESIDUAL_NUM = 16  # 레지듀얼 블록 수(오리지널: 19)
DN_INPUT_SHAPE = (8, 8, 2)  # 입력 셰이프
DN_OUTPUT_SIZE = 65  # 행동 수(배치 위치(6*6) + 패스(1))

# 학습 데이터 로드
def load_data():
    history = []
    for i in range(len(sorted(Path('./data/preprocessed_eg_data').glob('*.history')))):
        history_path = sorted(Path('./data/preprocessed_eg_data').glob('*.history'))[-1*(i+1)]
        with history_path.open(mode='rb') as f:
           history.extend(pickle.load(f))
    return history

# 컨볼루션 레이어 생성
def conv(filters):
    return Conv2D(filters, 3, padding='same', use_bias=False,
                  kernel_initializer='he_normal', kernel_regularizer=l2(0.0005))


# 레지듀얼 블록 생성
def residual_block():
    def f(x):
        sc = x
        x = conv(DN_FILTERS)(x)
        x = BatchNormalization()(x)
        x = Activation('relu')(x)
        x = conv(DN_FILTERS)(x)
        x = BatchNormalization()(x)
        x = Add()([x, sc])
        x = Activation('relu')(x)
        return x

    return f

# 입력 레이어
input = Input(shape=DN_INPUT_SHAPE)

# 컨볼루션 레이어
x = conv(DN_FILTERS)(input)
x = BatchNormalization()(x)
x = Activation('relu')(x)

# 레지듀얼 블록 x 16
for i in range(DN_RESIDUAL_NUM):
    x = residual_block()(x)

# 풀링 레이어
x = GlobalAveragePooling2D()(x)

# policy 출력
p = Dense(DN_OUTPUT_SIZE, kernel_regularizer=l2(0.0005),
          activation='softmax', name='pi')(x)

# value 출력
v = Dense(1, kernel_regularizer=l2(0.0005))(x)
v = Activation('tanh', name='v')(v)

# 모델 생성
model = Model(inputs=input, outputs=[p, v])

# 학습 데이터 로드
history = load_data()
xs, y_policies, y_values = zip(*history)

# 학습을 위한 입력 데이터 셰이프로 변환
a, b, c = DN_INPUT_SHAPE
xs = np.array(xs)
xs = xs.reshape(len(xs), c, a, b).transpose(0, 2, 3, 1)
y_policies = np.array(y_policies)
y_values = np.array(y_values)
    

# 모델 컴파일
model.compile(loss=['categorical_crossentropy', 'mse'], optimizer='adam')

# 학습률
def step_decay(epoch):
    x = 0.001
    if epoch >= 50: x = 0.0005
    if epoch >= 80: x = 0.00025
    return x

lr_decay = LearningRateScheduler(step_decay)

# 출력
print_callback = LambdaCallback(
    on_epoch_begin=lambda epoch, logs:
    print('\rTrain {}/{}'.format(epoch + 1, RN_EPOCHS), end=''))

# 학습 실행
model.fit(xs, [y_policies, y_values], batch_size=128, epochs=RN_EPOCHS,
          verbose=1, callbacks=[lr_decay, print_callback])
print('')

# 최신 플레이어 모델 저장
os.makedirs('./model/', exist_ok=True)  # 폴더가 없는 경우 생성
model.save('./model/inference_model.h5')

# 모델 파기
K.clear_session()
del model