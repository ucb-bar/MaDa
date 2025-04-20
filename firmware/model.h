#ifndef __MODEL_H
#define __MODEL_H

#include "nn.h"

// load the weight data block from the model.bin file
INCLUDE_FILE(".rodata", "./model.bin", model_weight);
extern uint8_t model_weight_data[];
extern size_t model_weight_start[];
extern size_t model_weight_end[];


uint8_t input_1_data[32];
uint8_t actor_0_data[16];
uint8_t actor_1_data[16];
uint8_t actor_2_data[8];
uint8_t actor_3_data[8];
uint8_t actor_4_data[4];
uint8_t output_data[4];

typedef struct {
  Tensor2D_F32 input_1;
  Tensor2D_F32 actor_0;
  Tensor2D_F32 actor_0_weight;
  Tensor1D_F32 actor_0_bias;
  Tensor2D_F32 actor_1;
  Tensor2D_F32 actor_2;
  Tensor2D_F32 actor_2_weight;
  Tensor1D_F32 actor_2_bias;
  Tensor2D_F32 actor_3;
  Tensor2D_F32 actor_4;
  Tensor2D_F32 actor_4_weight;
  Tensor1D_F32 actor_4_bias;
  Tensor2D_F32 output;
} Model;

void model_init(Model* model) {
  model->input_1.shape[0] = 1;
  model->input_1.shape[1] = 8;
  model->input_1.data = input_1_data;
  model->actor_0.shape[0] = 1;
  model->actor_0.shape[1] = 4;
  model->actor_0.data = actor_0_data;
  model->actor_0_weight.shape[0] = 4;
  model->actor_0_weight.shape[1] = 8;
  model->actor_0_weight.data = (float *)(model_weight_data + 0);
  model->actor_0_bias.shape[0] = 4;
  model->actor_0_bias.data = (float *)(model_weight_data + 128);
  model->actor_1.shape[0] = 1;
  model->actor_1.shape[1] = 4;
  model->actor_1.data = actor_1_data;
  model->actor_2.shape[0] = 1;
  model->actor_2.shape[1] = 2;
  model->actor_2.data = actor_2_data;
  model->actor_2_weight.shape[0] = 2;
  model->actor_2_weight.shape[1] = 4;
  model->actor_2_weight.data = (float *)(model_weight_data + 144);
  model->actor_2_bias.shape[0] = 2;
  model->actor_2_bias.data = (float *)(model_weight_data + 176);
  model->actor_3.shape[0] = 1;
  model->actor_3.shape[1] = 2;
  model->actor_3.data = actor_3_data;
  model->actor_4.shape[0] = 1;
  model->actor_4.shape[1] = 1;
  model->actor_4.data = actor_4_data;
  model->actor_4_weight.shape[0] = 1;
  model->actor_4_weight.shape[1] = 2;
  model->actor_4_weight.data = (float *)(model_weight_data + 184);
  model->actor_4_bias.shape[0] = 1;
  model->actor_4_bias.data = (float *)(model_weight_data + 192);
  model->output.shape[0] = 1;
  model->output.shape[1] = 1;
  model->output.data = output_data;
}

void model_forward(Model* model) {
  nn_linear_f32(&model->actor_0, &model->input_1, &model->actor_0_weight, &model->actor_0_bias);
  nn_relu2d_f32(&model->actor_1, &model->actor_0);
  nn_linear_f32(&model->actor_2, &model->actor_1, &model->actor_2_weight, &model->actor_2_bias);
  nn_relu2d_f32(&model->actor_3, &model->actor_2);
  nn_linear_f32(&model->actor_4, &model->actor_3, &model->actor_4_weight, &model->actor_4_bias);
  memcpy(model->output.data, model->actor_4.data, 4);
}

#endif  // __MODEL_H
