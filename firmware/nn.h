/**
 * @file nn.h
 * @brief The Baremetal-NN Library
 * 
 * This file contains the declarations of the functions and structures for the Baremetal-NN Library.
 */

#ifndef __NN_H
#define __NN_H

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <math.h>
#include <float.h>


// http://elm-chan.org/junk/32bit/binclude.html
#if defined(__APPLE__)
  #define INCLUDE_FILE(section, filename, symbol) asm (\
    ".align 4\n"                             /* Word alignment */\
    ".globl _"#symbol"_start\n"              /* Export the object start address */\
    ".globl _"#symbol"_data\n"               /* Export the object address */\
    "_"#symbol"_start:\n"                    /* Define the object start address label */\
    "_"#symbol"_data:\n"                     /* Define the object label */\
    ".incbin \""filename"\"\n"               /* Import the file */\
    ".globl _"#symbol"_end\n"                /* Export the object end address */\
    "_"#symbol"_end:\n"                      /* Define the object end address label */\
    ".align 4\n")                            /* Word alignment */
#elif defined(ARDUINO)
  #define INCBIN_PREFIX 
  #define INCBIN_STYLE INCBIN_STYLE_SNAKE
  #include "incbin.h"
  #define INCLUDE_FILE(section, filename, symbol) INCBIN(symbol, filename)
#else
  #define INCLUDE_FILE(section, filename, symbol) asm (\
    ".section "#section"\n"                   /* Change section */\
    ".balign 4\n"                             /* Word alignment */\
    ".global "#symbol"_start\n"               /* Export the object start address */\
    ".global "#symbol"_data\n"                /* Export the object address */\
    #symbol"_start:\n"                        /* Define the object start address label */\
    #symbol"_data:\n"                         /* Define the object label */\
    ".incbin \""filename"\"\n"                /* Import the file */\
    ".global "#symbol"_end\n"                 /* Export the object end address */\
    #symbol"_end:\n"                          /* Define the object end address label */\
    ".balign 4\n"                             /* Word alignment */\
    ".section \".text\"\n")                   /* Restore section */
#endif



/**
 * Tensor0D_F32
 *
 * @brief A 0D tensor (scalar) with a float data type.
 */
typedef struct {
  float data;
} Tensor0D_F32;


/**
 * Tensor1D_F32
 *
 * @brief A 1D tensor with a float data type.
 */
typedef struct {
  size_t shape[1];
  float *data;
} Tensor1D_F32;


/**
 * Tensor2D_F32
 *
 * @brief A 2D tensor with a float data type.
 */
typedef struct {
  size_t shape[2];
  float *data;
} Tensor2D_F32;

/**
 * Tensor3D_F32
 *
 * @brief A 3D tensor with a float data type.
 */
typedef struct {
  size_t shape[3];
  float *data;
} Tensor3D_F32;

/**
 * Tensor4D_F32
 *
 * @brief A 4D tensor with a float data type.
 */
typedef struct {
  size_t shape[4];
  float *data;
} Tensor4D_F32;



// /**
//  * nn_relu2d_f32
//  *
//  * @brief Applies the ReLU activation function to a 2D floating-point tensor.
//  *
//  * y[i][j] = max(x[i][j], 0)
//  *
//  * @param y The result tensor.
//  * @param x The input tensor.
//  */
// void nn_relu2d_f32(Tensor2D_F32 *y, const Tensor2D_F32 *x) {
//   // nn_assert(x->shape[0] == y->shape[0] && x->shape[1] == y->shape[1], "Cannot perform ReLU on tensors of different shapes");

//   size_t n = y->shape[1];
//   float *x_data = x->data;
//   float *y_data = y->data;

//   /* scalar implementation */
//   for (size_t i = 0; i < n; i += 1) {
//     float x_val = x_data[i];
//     y_data[i] = x_val > 0 ? x_val : 0;
//   }
// }


// /**
//  * nn_linear_f32
//  *
//  * @brief Linear neural network layer.
//  *
//  * y[i][j] = x[i][k] * weight[j][k] + bias[j]
//  *
//  * @param y The result tensor.
//  * @param x The input tensor.
//  * @param weight The weight tensor.
//  * @param bias The bias tensor.
//  */
// void nn_linear_f32(Tensor2D_F32 *y, const Tensor2D_F32 *x, const Tensor2D_F32 *weight, const Tensor1D_F32 *bias) {
//   // nn_assert(x->shape[1] == weight->shape[1], "Cannot perform Linear on tensors of different shapes");
//   // nn_assert(!bias || bias->shape[0] == weight->shape[0], "Cannot perform Linear on tensors of different shapes");
//   // nn_assert(y->shape[0] == x->shape[0] && y->shape[1] == weight->shape[0], "Cannot perform Linear on tensors of different shapes");

//   const size_t batch_size = x->shape[0];
//   const size_t in_features = x->shape[1];
//   const size_t out_features = weight->shape[0];

//   float *x_batch_data = x->data;
//   float *y_batch_data = y->data;

//   for (size_t i = 0; i < batch_size; i += 1) {
//     float *x_data = x_batch_data;
//     float *y_data = y_batch_data;

//     /* scalar implementation */
//     for (size_t j = 0; j < out_features; j += 1) {
//       float *weight_row = weight->data + j * in_features;

//       float sum = 0.f;
//       for (size_t k = 0; k < in_features; k += 1) {
//         sum += x_data[k] * weight_row[k];
//       }
//       if (bias) {
//         sum += bias->data[j];
//       }
//       y_data[j] = sum;
//     }

//     x_batch_data += in_features;
//     y_batch_data += out_features;
//   }
// }


#endif // __NN_H