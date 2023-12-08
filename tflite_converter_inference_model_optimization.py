import tensorflow as tf
from keras.models import load_model

model = load_model("./model/inference_model.h5")

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_quant_model = converter.convert()
open("./model/quantized_inference_model_int8.tflite", "wb").write(tflite_quant_model)