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

JNIEXPORT jstring JNICALL Java_com_example_comsyslabAssignment2_Native_MensaJNILib_handleRequest( JNIEnv* env,
                                                  jobject thiz, jstring jniHostname, jstring jniRequest)
{
	int socketId;
	int buffer_length = 20480;
	char response[buffer_length];
	int length;
	int total_received, bytes_received = 0;
	char *endingTag= "</html>";
	// Convert JNI String into char *
	const char* request = (*env)->GetStringUTFChars(env, jniRequest, NULL);
   	if (NULL == request) return NULL;
   	const char* hostname = (*env)->GetStringUTFChars(env, jniHostname, NULL);
   	if(NULL == hostname) return NULL;

   	socketId = setUpSocket(hostname); // Create new socket and configure it

   	sendto(socketId, request, strlen(request),0,  NULL, 0); // Sent HTTP request
   	// Read while there is space on the buffer or until the termination HTML tag has been received.
   	LOGD("About to receive.");
   	LOGD("total_received = %d\nfind endingTag = %d\noriginal str length = %d\n",total_received,strstr(response,endingTag) != NULL,strlen(response));
   	while(((bytes_received = recv(socketId, ((uint8_t *)response)+total_received, buffer_length - total_received, 0)) > 0) && (strstr(response,endingTag) == NULL))
   	{
   		LOGD("Bytes received = %d",bytes_received);
   		total_received = bytes_received +total_received;
    }
   	length = strlen(response);
   	LOGD("Response length: %d\nTotal received: %d\n",length,total_received);

   	close(socketId);

    return (*env)->NewStringUTF(env, response); // Convert char * back to Java String.
}
/*
	Function used to initialize the socket using IP protocol, TCP method and a destination hostname/port.
*/
int setUpSocket(char* hostname)
{
	struct addrinfo hints;
    struct addrinfo *result, *rp;
    int sfd, s;

   /* Obtain address(es) matching host/port */

   memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_INET;    /* Allow IPv4 */
    hints.ai_socktype = SOCK_STREAM; /* TCP socket */
    hints.ai_flags = 0;
    hints.ai_protocol = 0;          /* Any protocol */

   s = getaddrinfo(hostname, "80", &hints, &result);
   if (s != 0) {
        LOGE("getaddrinfo: %s\n", gai_strerror(s));
   }

   /* getaddrinfo() returns a list of address structures.
       Try each address until we successfully connect(2).
       If socket(2) (or connect(2)) fails, we (close the socket
       and) try the next address. */

   for (rp = result; rp != NULL; rp = rp->ai_next) {
        sfd = socket(rp->ai_family, rp->ai_socktype,
                     rp->ai_protocol);
        if (sfd == -1)
            continue;

       if (connect(sfd, rp->ai_addr, rp->ai_addrlen) != -1)
            break;                  /* Success */

       close(sfd);
    }

   if (rp == NULL) {               /* No address succeeded */
        LOGE("Could not connect\n");
   }
	struct timeval tv;
	tv.tv_sec = 3;  /* 3 Secs Timeout */
	setsockopt(sfd, SOL_SOCKET, SO_RCVTIMEO,(char *)&tv, sizeof(struct timeval)); // Assign 3 seconds timeout to socket

    freeaddrinfo(result);           /* No longer needed */
	return sfd;
}