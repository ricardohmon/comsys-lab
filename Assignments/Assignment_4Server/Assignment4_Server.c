#include <time.h>
#include <stdint.h>
#include <string.h>
#include "Assignment4_Server.h"
#include <errno.h>

int  mem_fd;
void *gpio_map;

// I/O access
volatile unsigned *gpio;

struct timespec sleeptime;
struct plug_node * headerPlug;
int socketId;
//
// Set up a memory regions to access GPIO
//
void setup_io()
{
   /* open /dev/mem */
   if ((mem_fd = open("/dev/mem", O_RDWR|O_SYNC) ) < 0) {
      printf("can't open /dev/mem \n");
      exit(-1);
   }

   /* mmap GPIO */
   gpio_map = mmap(
      NULL,             //Any adddress in our space will do
      BLOCK_SIZE,       //Map length
      PROT_READ|PROT_WRITE,// Enable reading & writting to mapped memory
      MAP_SHARED,       //Shared with other processes
      mem_fd,           //File to map
      GPIO_BASE         //Offset to GPIO peripheral
   );

   close(mem_fd); //No need to keep mem_fd open after mmap

   if (gpio_map == MAP_FAILED) {
      printf("mmap error %d\n", (int)gpio_map);//errno also set!
      exit(-1);
   }

   // Always use volatile pointer!
   gpio = (volatile unsigned *)gpio_map;
   printf("SETUP FINISHED\n");
} // setup_io


/* this will register an contructor that calls the setup_io() when the app starts */
static void con() __attribute__((constructor));

void con() {
    setup_io();
}



/* use this to send power to a pin */
void set_pin(int pin) {
	GPIO_SET = 1<<pin;
}

/* use this to set no power to a pin */
void clr_pin(int pin) {
	GPIO_CLR = 1<<pin;
}


int main(int argc, char** argv)
{

    // this is called after the contructor!
    
    if(argc < 2)
    {
      printf("Expecting server's port number\n");
      return 1;
    }

    if(argc < 3)
    {
      printf("Expecting configuration filename\n");
      return 1;
    }

    headerPlug = malloc(sizeof(struct plug_node));
    printf("Reading configuration\n");
    readConfiguration(argv[2]);
    printf("Plugs found: %d\n",getListlength(headerPlug));

    printf("About to configure GPIO\n");
    // you must run this as root!!!
    // Configure GPIO 17 as output

    INP_GPIO(17);
    OUT_GPIO(17);
    
    socketId = setUpSocket(atoi(argv[1]));
    listenForData(socketId);
    
    /*
    plug_local_info * plug = &headerPlug->plug;
    sendSequence(plug->plug_code,TYPE_SWITCH_ON);
    sendSequence(plug->plug_code,TYPE_SWITCH_ON);
    sendSequence(plug->plug_code,TYPE_SWITCH_ON);
    */
    close(socketId);
}

/*

  Read configuration file and create a list of plugs

  */
void readConfiguration(char * filename) {
  FILE * file;
  char line[50];
  struct plug_node * plugNodePtr = headerPlug;
  struct plug_node * lastPlugNodePtr = plugNodePtr;
  // Try to open file, exit if not possible.
  file = fopen(filename,"r");
  if (file == NULL)
  {
    error("Not able to open file.");
  }

  // Read every line, check if start with expected prefix and add new plug info into the list.
  while(fgets(line,sizeof(line),file) != NULL)
  {
      if(strncmp(line,PLUG_PREFIX,strlen(PLUG_PREFIX)) == 0)
      {
          if(plugNodePtr != lastPlugNodePtr) {
            plugNodePtr = malloc(sizeof(struct plug_node));
            lastPlugNodePtr->next = plugNodePtr;
          }
          char name[40];
          // Extract Plud Id, name, Plug code and if present a comment.
          sscanf(line,"PLUG %hhu %s %hx %s",
                        &plugNodePtr->plug.plugId.id,
                        name,
                        &plugNodePtr->plug.plug_code);
          plugNodePtr->plug.status.status = TYPE_STATUS_OFF;
          plugNodePtr->plug.name_length = strlen(name);
          plugNodePtr->plug.name = malloc(plugNodePtr->plug.name_length);
          strcpy(plugNodePtr->plug.name,name);
          lastPlugNodePtr = plugNodePtr;
          plugNodePtr = plugNodePtr->next;
      }
  }
}
/*
    
    Retrieve the number of nodes in the list.

*/
int getListlength(struct plug_node * headernode) {
  int count = 0;
  struct plug_node * nodePtr = headernode;
  while(nodePtr != NULL) {
    nodePtr = nodePtr->next;
    count++;
  }
  return count;
}
/*

    Retrieve Plug code by searching for the plug id in the list of plugs.

*/
unsigned short findPlugCode(uint8_t id)
{
  uint16_t plug_code = 0;
  struct plug_node * nodePtr = headerPlug;
  while(nodePtr != NULL) {
    if(nodePtr->plug.plugId.id == id)
    {
      plug_code = nodePtr->plug.plug_code;
      break;
      nodePtr = nodePtr->next;
    }
  }
  return plug_code;
}
/*
  
    Returns a byte array containing the message with the list of plugs configured.

*/
uint8_t * getPlugListMessage()
{
  struct plug_node * nodePtr = headerPlug;
  int message_size = 0;
  int bufferIdx = 0;
  message_size = calculateMessageSize();
  uint8_t * buffer = malloc(message_size);
  
  buffer[bufferIdx++] = TYPE_PLUG_LIST;
  buffer[bufferIdx++] = (uint8_t)getListlength(headerPlug);
  while(nodePtr != NULL) {
    buffer[bufferIdx++] = nodePtr->plug.plugId.id;
    buffer[bufferIdx++] = nodePtr->plug.status.status;
    buffer[bufferIdx++] = nodePtr->plug.name_length;
    memcpy(&buffer[bufferIdx],nodePtr->plug.name,nodePtr->plug.name_length);
    bufferIdx += nodePtr->plug.name_length;
    nodePtr = nodePtr->next;
  }
  return buffer;
}
int calculateMessageSize()
{
  int message_size = 2;
  struct plug_node * nodePtr = headerPlug;
  while(nodePtr != NULL) {
    message_size += 3;
    message_size += nodePtr->plug.name_length;
    nodePtr = nodePtr->next;
  }
  return message_size;
}
/*
  
  Initialize a UDP socket and bind it to the current address.

*/
int setUpSocket(unsigned short serverPort)
{
  struct sockaddr_in serv_addr;
  memset(&serv_addr, 0, sizeof(struct sockaddr_in)); 
  serv_addr.sin_family = AF_INET; // Select Internet address family
  serv_addr.sin_port = htons(serverPort); // Set the port following the network's presentation
  serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
  socketId = socket(PF_INET,SOCK_DGRAM,0); // Create UDP socket
  if (socketId < 0) 
        printf("ERROR opening socket");
  struct timeval tv;
  tv.tv_sec = 3;  /* 3 Secs Timeout */
  setsockopt(socketId, SOL_SOCKET, SO_RCVTIMEO,(char *)&tv, sizeof(struct timeval)); // Assign 3 seconds timeout to socket
  if (bind(socketId,(struct sockaddr *)&serv_addr, sizeof(struct sockaddr_in)) < 0) // Establish connection using the IP address
        printf("ERROR binding socket");
  printf("Socket created on port: %hu\n",serverPort);
  return socketId;
}

void listenForData(int socketId){
    struct sockaddr_in sourceAddr;
    uint8_t buffer[256];
    unsigned short sourcePort;
    unsigned long sourceAddress;
    socklen_t length=sizeof(sourceAddr);
    int count;
    do {
      memset(buffer, 0, sizeof(buffer)); 
      printf("Listening for data\n");
      count = recvfrom(socketId,buffer,sizeof(buffer),0, (struct sockaddr *) &sourceAddr, &length);
      if(count < 0) {
        printf("ERROR while receiving data. Errno: %d",errno);
        count = 0;
      }
      printf("Bytes received: %d\n",count);
      handleRequest(buffer,sourceAddr);
    } while(1);
}
void sendPackage(uint8_t * buffer, int size, struct sockaddr_in sourceAddr) 
{
  struct sockaddr_in serv_addr;
  int count;

  memset(&serv_addr, 0, sizeof(struct sockaddr_in)); 
  serv_addr.sin_family = AF_INET; // Select Internet address family
  //serv_addr.sin_port = htons(sourcePort); // Set the port following the network's presentation
  //inet_aton(hostname,(struct in_addr *)&serv_addr.sin_addr); // Assign IP Address in the corresponding representation
  //serv_addr.sin_addr.s_addr = sourceAddress;
  //LOGD("About to send. Length: %d, Port: %hu, IP: %s",length,sourcePort,hostname);
  count = sendto(socketId, buffer, size,0,  (struct sockaddr *) &sourceAddr, sizeof(struct sockaddr_in)); // Sent request
  
  printf("Total sent = %d\n",count);

}
void handleRequest(uint8_t * buffer, struct sockaddr_in sourceAddr)
{
  uint8_t message_type;
  uint8_t * message;
  int message_size;
  uint8_t plugId;

  if(buffer != NULL) {
    message_type = buffer[0];

    switch(message_type) {
      case TYPE_DISCOVER : 
            message = getPlugListMessage();
            message_size = calculateMessageSize();
            sendPackage(message, message_size, sourceAddr);
            break;
      case TYPE_SWITCH_ON :
            printf("Switch on\n");
            plugId = buffer[1];
            sendSequenceByPlugId(plugId,TYPE_SWITCH_ON);
            break;
      case TYPE_SWITCH_OFF : 
            printf("Switch off\n");
            plugId = buffer[1];
            sendSequenceByPlugId(plugId,TYPE_SWITCH_OFF);
            break;
    }
    //free(buffer);
  }
}
/*

  Send the corresponding wave signal based on the plug id and the action.

*/
void sendSequenceByPlugId(uint8_t id, int action)
{
  uint16_t plug_code = 0;
  plug_code = findPlugCode(id);
  if(plug_code != 0) {
    sendSequence(plug_code, action);
    sendSequence(plug_code, action);
    sendSequence(plug_code, action);
  }
}
/*

  Send the corresponding wave signal based on the plug code and the action.

*/
void sendSequence(unsigned short plug_code, int action)
{
    /*
     Start the sequence
     Send House code + Switch code + ON/OFF code + Sync code
     */
     // Validate plug code is different than zero.
    if (plug_code) {
      printf("%hx\n", plug_code);
      unsigned short idx = 0x0400;
      // Check every bit of the code and send the correspondent signal. 
      // At the end, send the desired action, followed with the 
      // synchronization sequence.
      do {
        idx = idx>>1;
        if(plug_code & idx) {
          sendHigh();
        }
        else
        {
          sendLow();
        }
      } while(!(idx & 0x0001));
    
      if(action == TYPE_SWITCH_ON)
      {
        sendOn();
      }
      else
      {
        sendOff();
      }
      sendSync();
    }
}

void sendHigh()
{
    set_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X1;
    nanosleep(&sleeptime,NULL);

    clr_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X3;
    nanosleep(&sleeptime,NULL);

    set_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X1;
    nanosleep(&sleeptime,NULL);

    clr_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X3;
    nanosleep(&sleeptime,NULL);
}
void sendLow()
{
    set_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X1;
    nanosleep(&sleeptime,NULL);

    clr_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X3;
    nanosleep(&sleeptime,NULL);

    set_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X3;
    nanosleep(&sleeptime,NULL);

    clr_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X1;
    nanosleep(&sleeptime,NULL);
}
void sendOn()
{
    sendHigh();
    sendLow();
}
void sendOff()
{
    sendLow();
    sendHigh();
}
void sendSync()
{
    set_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X1;
    nanosleep(&sleeptime,NULL);
    clr_pin(DATA_PIN);
    sleeptime.tv_nsec = SLOT_TIME_X31;
    nanosleep(&sleeptime,NULL);
}

void error(char *msg)
{
    perror(msg);
    exit(0);
}
