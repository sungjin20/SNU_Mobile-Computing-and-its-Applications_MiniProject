import tensorflow as tf
import numpy as np
from keras.models import load_model

test_images = np.load('./data/test_data.npy').astype('float32')
test_labels = np.load('./data/test_labels.npy').astype('uint8')

model = load_model("./model/recognition_model.h5")
def representative_data_gen():
  for input_value in tf.data.Dataset.from_tensor_slices(test_images).batch(1).take(100):
    yield [input_value]

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.representative_dataset = representative_data_gen
tflite_quant_model = converter.convert()
open("./model/quantized_recognition_model_int8.tflite", "wb").write(tflite_quant_model)


def evaluate_model(interpreter):
  input_index = interpreter.get_input_details()[0]["index"]
  output_index = interpreter.get_output_details()[0]["index"]
  prediction_digits = []
  for i, test_image in enumerate(test_images):
    if i % 100 == 0:
      print('Evaluated on {n} results so far.'.format(n=i))
    test_image = np.expand_dims(test_image, axis=0).astype(np.float32)
    interpreter.set_tensor(input_index, test_image)
    interpreter.invoke()
    output = interpreter.tensor(output_index)
    digit = np.argmax(output()[0])
    prediction_digits.append(digit)

  print('\n')
  prediction_digits = np.array(prediction_digits)
  prediction_digits = prediction_digits.reshape(prediction_digits.shape[0], 1)
  accuracy = (prediction_digits == test_labels).mean()
  return accuracy

interpreter = tf.lite.Interpreter(model_content=tflite_quant_model)
interpreter.allocate_tensors()

test_accuracy = evaluate_model(interpreter)

print('Quant TFLite test_accuracy:', test_accuracy)
