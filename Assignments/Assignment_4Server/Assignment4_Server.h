#include <time.h>
#include <stdint.h>
#include "Assignment4_network.h"
#include <netinet/in.h>
//#include <bcm2835.h>

// Access from ARM Running Linux

#define BCM2708_PERI_BASE        0x20000000
#define GPIO_BASE                (BCM2708_PERI_BASE + 0x200000) /* GPIO controller */


#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>

#define PAGE_SIZE (4*1024)
#define BLOCK_SIZE (4*1024)

// GPIO setup macros. Always use INP_GPIO(x) before using OUT_GPIO(x) or SET_GPIO_ALT(x,y)
#define INP_GPIO(g) *(gpio+((g)/10)) &= ~(7<<(((g)%10)*3))
#define OUT_GPIO(g) *(gpio+((g)/10)) |=  (1<<(((g)%10)*3))
#define SET_GPIO_ALT(g,a) *(gpio+(((g)/10))) |= (((a)<=3?(a)+4:(a)==4?3:2)<<(((g)%10)*3))

#define GPIO_SET *(gpio+7)  // sets   bits which are 1 ignores bits which are 0
#define GPIO_CLR *(gpio+10) // clears bits which are 1 ignores bits which are 0

#define DATA_PIN 17
#define SLOT_TIME_X1 370000 // 370 us in nanoseconds
#define SLOT_TIME_X3 SLOT_TIME_X1*3 // 370 us in nanoseconds
#define SLOT_TIME_X31 SLOT_TIME_X1*31 // 370 us in nanoseconds

typedef struct {
  plugId_struct plugId;
  uint16_t plug_code;
  plug_status_struct status;
  uint8_t name_length;
  char * name;
} plug_local_info;

struct plug_node {
  plug_local_info plug;
  struct plug_node * next;
};

const char PLUG_PREFIX[] = "PLUG";

void setup_io();
void sendSequence(unsigned short , int );
void sendSequenceByPlugId(uint8_t , int );
void sendHigh();
void sendLow();
void sendOn();
void sendOff();
void sendSync();
void readConfiguration(char * );
int setUpSocket(unsigned short);
void listenForData(int);
void sendPackage(uint8_t * , int, const struct sockaddr_in);
void handleRequest(uint8_t * , const struct sockaddr_in);
int getListlength(struct plug_node * );
unsigned short findPlugCode(uint8_t);
uint8_t * getPlugListMessage();
int calculateMessageSize();
void error(char *);

