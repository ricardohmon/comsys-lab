#include <android/log.h>

#include <string.h>
#include <jni.h>

#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
// Headers required for working with sockets
#include <netinet/in.h>
#include <arpa/inet.h>

#define  LOG_TAG    "native_socket"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__) // Define debugging logs to LogCat
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__) // Define error logs to LogCat


#define TYPE_REGISTER 1
#define TYPE_UNREGISTER 2
#define TYPE_KEEPALIVE 3
#define TYPE_EVENT 4
#define TYPE_SHAKE 5

typedef struct {
  uint8_t type;
  uint8_t len_name;
} __attribute__ ((__packed__)) register_struct;

typedef struct {
  uint8_t type;
} __attribute__ ((__packed__)) type_struct;

typedef struct {
  uint8_t type;
  uint64_t timestamp;
} __attribute__ ((__packed__)) event_struct;

typedef struct {
  uint8_t type;
  uint64_t timestamp;
  uint8_t len_name;
};
const short SERVER_PORT = 2345;

JNIEXPORT void JNICALL Java_com_example_comsyslabAssignment3_Native_ClientJNILib_register( JNIEnv* env,
                                                  jobject thiz, jstring jniHostname, jstring jniRequest)
{
  register_struct registerRequest;
	uint8_t * buffer;
  size_t name_length;
	// Convert JNI String into char *
	const char* request = (*env)->GetStringUTFChars(env, jniRequest, NULL);
 	if (NULL == request) return NULL;
 	const char* hostname = (*env)->GetStringUTFChars(env, jniHostname, NULL);
 	if(NULL == hostname) return NULL;

  registerRequest.type = TYPE_REGISTER;
  name_length = strlen(request);
  buffer = malloc(name_length + sizeof(register_struct));
  registerRequest.len_name = name_length;
  memcpy(buffer,&registerRequest,sizeof(register_struct));
  memcpy(buffer + sizeof(register_struct),request,name_length);
  LOGD("Type: %c",buffer[0]);
  sendPackage(hostname,buffer,sizeof(register_struct) + name_length);
  free(buffer);
}
JNIEXPORT void JNICALL Java_com_example_comsyslabAssignment3_Native_ClientJNILib_unregister( JNIEnv* env,
                                                  jobject thiz, jstring jniHostname)
{
  type_struct unregisterRequest;
  
  const char* hostname = (*env)->GetStringUTFChars(env, jniHostname, NULL);
  if(NULL == hostname) return NULL;

  unregisterRequest.type = TYPE_UNREGISTER;
  
  sendPackage(hostname,&unregisterRequest,sizeof(type_struct));
}
JNIEXPORT void JNICALL Java_com_example_comsyslabAssignment3_Native_ClientJNILib_keepAlive( JNIEnv* env,
                                                  jobject thiz, jstring jniHostname)
{
  type_struct keepAliveRequest;
  
  const char* hostname = (*env)->GetStringUTFChars(env, jniHostname, NULL);
  if(NULL == hostname) return NULL;

  keepAliveRequest.type = TYPE_KEEPALIVE;
  
  sendPackage(hostname,&keepAliveRequest,sizeof(type_struct));
}
JNIEXPORT void JNICALL Java_com_example_comsyslabAssignment3_Native_ClientJNILib_sendEvent( JNIEnv* env,
                                                  jobject thiz, jstring jniHostname)
{
  event_struct eventNotification;
  size_t name_length;
  
  const char* hostname = (*env)->GetStringUTFChars(env, jniHostname, NULL);
  if(NULL == hostname) return NULL;

  eventNotification.type = TYPE_EVENT;
  eventNotification.timestamp = htonl(time(NULL));

  sendPackage(hostname,&eventNotification,sizeof(event_struct));
}
void sendPackage(char* ipAddress, uint8_t * buffer, int length) 
{
  int socketId;
  struct sockaddr_in serv_addr;
  int count;

  socketId = setUpSocket(ipAddress); // Create new socket and configure it

  memset(&serv_addr, 0, sizeof(struct sockaddr_in)); 
  serv_addr.sin_family = AF_INET; // Select Internet address family
  serv_addr.sin_port = htons(SERVER_PORT); // Set the port following the network's presentation
  inet_aton(ipAddress,(struct in_addr *)&serv_addr.sin_addr); // Assign IP Address in the corresponding representation

  LOGD("About to send. Length: %d, Port: %hu, IP: %s",length,SERVER_PORT,ipAddress);
  count = sendto(socketId, buffer, length,0,  (struct sockaddr *) &serv_addr, sizeof(struct sockaddr_in)); // Sent request
  
  LOGD("Total sent = %d\n",count);

  close(socketId);
}

/*
	Function used to initialize the socket using IP protocol, UDP method and a destination IP address/port.
*/
int setUpSocket(char* ipAddress)
{
  struct sockaddr_in serv_addr;
  memset(&serv_addr, 0, sizeof(struct sockaddr_in)); 
  serv_addr.sin_family = AF_INET; // Select Internet address family
  serv_addr.sin_port = htons(SERVER_PORT); // Set the port following the network's presentation
  serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
  //inet_aton(ipAddress,(struct in_addr *)&serv_addr.sin_addr); // Assign IP Address in the corresponding representation
  int socketId = socket(PF_INET,SOCK_DGRAM,0); // Create UDP socket
  if (socketId < 0) 
        LOGE("ERROR opening socket");
  struct timeval tv;
  tv.tv_sec = 3;  /* 3 Secs Timeout */
  setsockopt(socketId, SOL_SOCKET, SO_RCVTIMEO,(char *)&tv, sizeof(struct timeval)); // Assign 3 seconds timeout to socket
  if (bind(socketId,(struct sockaddr *)&serv_addr, sizeof(struct sockaddr_in)) < 0) // Establish connection using the IP address
        LOGE("ERROR binding socket");
  return socketId;
}