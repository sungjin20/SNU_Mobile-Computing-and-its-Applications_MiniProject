from tensorflow.keras.callbacks import LearningRateScheduler
from tensorflow.keras.layers import Activation, Add, BatchNormalization, Conv2D, Dense, GlobalAveragePooling2D, Input
from tensorflow.keras.models import Model
from tensorflow.keras.optimizers import SGD
from tensorflow.keras.regularizers import l2
from tensorflow.keras.utils import to_categorical
import numpy as np

train_images = np.load('./data/train_data.npy').astype('float32')
test_images = np.load('./data/test_data.npy').astype('float32')
train_labels = np.load('./data/train_labels.npy').astype('uint8')
test_labels = np.load('./data/test_labels.npy').astype('uint8')
train_labels = to_categorical(train_labels)
test_labels = to_categorical(test_labels)

def conv(filters, kernel_size, strides=1):
    return Conv2D(filters, kernel_size, strides=strides, padding='same', use_bias=False,
        kernel_initializer='he_normal', kernel_regularizer=l2(0.0001))

def first_residual_unit(filters, strides):
    def f(x):
        x = BatchNormalization()(x)
        b = Activation('relu')(x)
        x = conv(filters // 4, 1, strides)(b)
        x = BatchNormalization()(x)
        x = Activation('relu')(x)
        x = conv(filters // 4, 3)(x)
        x = BatchNormalization()(x)
        x = Activation('relu')(x)
        x = conv(filters, 1)(x)
        sc = conv(filters, 1, strides)(b)
        return Add()([x, sc])
    return f

def residual_unit(filters):
    def f(x):
        sc = x
        x = BatchNormalization()(x)
        x = Activation('relu')(x)
        x = conv(filters // 4, 1)(x)
        x = BatchNormalization()(x)
        x = Activation('relu')(x)
        x = conv(filters // 4, 3)(x)
        x = BatchNormalization()(x)
        x = Activation('relu')(x)
        x = conv(filters, 1)(x)
        return Add()([x, sc])
    return f

def residual_block(filters, strides, unit_size):
    def f(x):
        x = first_residual_unit(filters, strides)(x)
        for i in range(unit_size-1):
            x = residual_unit(filters)(x)
        return x
    return f

def step_decay(epoch):
    x = 0.1
    if epoch >= 80: x = 0.01
    return x
lr_decay = LearningRateScheduler(step_decay)


input = Input(shape=(32, 32, 3))
x = conv(16, 3)(input)
x = residual_block(64, 1, 18)(x)
x = residual_block(128, 2, 18)(x)
x = residual_block(256, 2, 18)(x)
x = BatchNormalization()(x)
x = Activation('relu')(x)
x = GlobalAveragePooling2D()(x)
output = Dense(3, activation='softmax', kernel_regularizer=l2(0.0001))(x)
model = Model(inputs=input, outputs=output)

model.compile(loss='categorical_crossentropy', optimizer=SGD(momentum=0.9), metrics=['acc'])

batch_size = 128
history = model.fit(train_images, train_labels, batch_size=batch_size,
                    epochs=100,
                    steps_per_epoch=train_images.shape[0] // batch_size,
                    validation_data = (test_images, test_labels),
                    validation_steps=test_images.shape[0] // batch_size,
                    callbacks=[lr_decay],
                    verbose = 1)
model.save('./model/recognition_model.h5')