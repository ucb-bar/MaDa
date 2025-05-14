/**
 * @file nn.h
 * @brief The Baremetal-NN Library
 * 
 * This file contains the declarations of the functions and structures for the Baremetal-NN Library.
 */

#ifndef __NN_MINI_H
#define __NN_MINI_H


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
  uint32_t data;
} Tensor0D_F32;


/**
 * Tensor1D_F32
 *
 * @brief A 1D tensor with a float data type.
 */
typedef struct {
  size_t shape[1];
  uint32_t *data;
} Tensor1D_F32;


/**
 * Tensor2D_F32
 *
 * @brief A 2D tensor with a float data type.
 */
typedef struct {
  size_t shape[2];
  uint32_t *data;
} Tensor2D_F32;




#endif // __NN_MINI_H