#ifndef __MODEL_H
#define __MODEL_H

#include "nn.h"

// load the weight data block from the model.bin file
INCLUDE_FILE(".rodata", "./weights.bin", model_weight);
extern uint8_t model_weight_data[];
extern size_t model_weight_start[];
extern size_t model_weight_end[];


static uint8_t zero[4] __attribute__((aligned(16)));
static uint8_t y_data[16] __attribute__((aligned(16)));
static uint8_t b_data[16] __attribute__((aligned(16)));
static uint8_t x_data[16] __attribute__((aligned(16)));

typedef struct {
  Tensor1D_F32 x;
  Tensor2D_F32 w;
  Tensor1D_F32 b;
  Tensor1D_F32 y;
} Model __attribute__((aligned(16)));

void model_init(Model* model) {
  model->x.shape[0] = 3;
  model->x.data = x_data;
  // model->w.shape[0] = 4;
  // model->w.shape[1] = 3;
  // model->w.data = (float *)(model_weight_data + 0);
  // model->b.shape[0] = 4;
  // model->b.data = (float *)(model_weight_data + 48);
  // model->y.shape[0] = 4;
  // model->y.data = y_data;
}

void model_forward(Model* model) {
  // nn_linear_f32(&model->y, &model->x, &model->w, &model->b);
}

#endif  // __MODEL_H
