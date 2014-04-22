/* 

	Communication Systems Lab
	Assignment 1
	Topic: Socket Programming
	TCP Server implementation

*/
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h> // Required to declare exit() function
#include <unistd.h> // Required to declare close() function
#include <stdio.h> // Required to declare perror() function
#include <string.h> // Required to declare memset(...) function
#include <pthread.h>
#define JOKER_REQUEST_TYPE 1
#define JOKER_RESPONSE_TYPE 2

typedef struct {
	uint8_t type;
	uint8_t len_first_name;
	uint8_t len_last_name;
} __attribute__ ((__packed__)) joker_request;

typedef struct {
	uint8_t type;
	uint32_t len_joke;
} __attribute__ ((__packed__)) joker_response;

const short SERVER_PORT = 2345;
pthread_mutex_t comm_lock;

int setUpSocket();
void listenForRequests();
void error(char *msg);
char * makeUpJoke();
void *HandleConnection(void *clientSocketID);

int main() {
	int serverSocketID;
	serverSocketID = setUpSocket();
	listenForRequests(serverSocketID);
}
/*
	Function used to initialize the socket using IP protocol, TCP method and a pre-defined port.
*/
int setUpSocket()
{
	int serverSocketID;
	struct sockaddr_in serverSocketAddr;

	serverSocketID = socket(PF_INET, SOCK_STREAM, 0); // Create socket for TCP connections
	if (serverSocketID < 0) 
        error("ERROR opening socket");
	memset(&serverSocketAddr, 0, sizeof(struct sockaddr_in));
	serverSocketAddr.sin_family = AF_INET; // Select Internet address family
	serverSocketAddr.sin_addr.s_addr = htonl(INADDR_ANY); // Select any interface
	serverSocketAddr.sin_port = htons(SERVER_PORT); // Set the port following the network's presentation
	if(bind(serverSocketID, (struct sockaddr *)&serverSocketAddr, sizeof(struct sockaddr_in)) < 0) // Bind socket to an address.
	{
		error("ERROR binding socket");
	}

	return serverSocketID;
}
/*
	Function which handles requests and responds to clients.
*/
void listenForRequests(int serverSocketID)
{
	struct sockaddr_in clientSocketAddr;
	uint32_t addrLen = sizeof(struct sockaddr_in);
	pthread_t threadID;
	//Set up mutex for communication channel
	if(pthread_mutex_init(&comm_lock,NULL) != 0) {
		error("ERROR initialiazing mutex");
	}
	// Start listening on the socket created before.
	if(listen(serverSocketID, 10) < 0)
	{
		error("ERROR starting to listen");
	}
	printf("Server listening on port %d\n", SERVER_PORT);
	do {
		int clientSocketID;
		clientSocketID = accept(serverSocketID,(struct sockaddr *) &clientSocketAddr, &addrLen); // Accept an incoming request.
		if (pthread_create (&threadID, NULL, HandleConnection, (void *)&clientSocketID) != 0)
			perror("ERROR creating thread");
	} while(1);
}
void *HandleConnection(void *arg)
{
	int *clientSocketID = arg;
	int rcvCount,sentCount,totalRcvd = 0;
	char *first_name, *last_name, *joke;
	joker_request jokerRequest;
	joker_response jokerResponse;
	uint8_t buffer_size;
	char *buffer;
	printf("Connection accepted.\n");
	memset(&jokerRequest, 0, sizeof(jokerRequest)); // Fill the request's memory space with zeros.
	pthread_mutex_lock(&comm_lock); // Block access to resource
	if((rcvCount = recv(*clientSocketID, ((uint8_t *)&jokerRequest), sizeof(joker_request), 0)) < 0) // Read first the joke request's header
	{
		perror("ERROR receiving");
	}
	pthread_mutex_unlock(&comm_lock);
	printf("Length of first name: %d\n", jokerRequest.len_first_name);
	printf("Length of last name: %d\n", jokerRequest.len_last_name);
	buffer_size = jokerRequest.len_first_name + jokerRequest.len_last_name;
	buffer = malloc(buffer_size); // Allocate some space to the buffer which will receive the first and last names
	first_name = malloc(jokerRequest.len_first_name + 1); // Allocate space to the first name according to the information received in the header.
	last_name = malloc(jokerRequest.len_last_name + 1); // Allocate space to the last name according to the information received in the header.
	do { // Receive from the socket as many characters as stated in the header.
		pthread_mutex_lock(&comm_lock);
		if((rcvCount = recv(*clientSocketID, buffer + totalRcvd, buffer_size - totalRcvd, 0)) < 0)
		{
			perror("ERROR receiving");
		}
		pthread_mutex_unlock(&comm_lock);
		printf("Bytes received: %d\n",rcvCount);
		totalRcvd += rcvCount;
	} while(totalRcvd < buffer_size);
	// Once received the complete name, copy each of them to the corresponding variables and add a end-of-string character.
	memcpy(first_name,buffer,jokerRequest.len_first_name);
	memcpy(last_name,&buffer[strlen(first_name)],jokerRequest.len_last_name);
	first_name[jokerRequest.len_first_name] = '\0';
	last_name[jokerRequest.len_last_name] = '\0';
	printf("First name: %s \nLast name: %s\n",first_name,last_name);
	joke = makeUpJoke(first_name,last_name); // Call the function in charge of creating a new joke and fill the header structure
	jokerResponse.type = JOKER_RESPONSE_TYPE;
	jokerResponse.len_joke = htonl(strlen(joke));
	pthread_mutex_lock(&comm_lock);
	if((sentCount = sendto(*clientSocketID, &jokerResponse, sizeof(joker_response),0,  NULL, 0)) < 0)
	{
		perror("ERROR sending");
	}
	pthread_mutex_unlock(&comm_lock);
	printf("Characters sent: %d\n", sentCount); // Send the joke
	pthread_mutex_lock(&comm_lock);
	if((sentCount = sendto(*clientSocketID, joke, strlen(joke),0,  NULL, 0)) < 0)
	{
		perror("ERROR sending");
	}
	pthread_mutex_unlock(&comm_lock);
	printf("Characters sent: %d\n", sentCount);
	sleep(30); // Threads demo
	// Free the resources and close the client's socket.
	free(joke);
	free(first_name);
	free(last_name);
	free(buffer);
	close(*clientSocketID);
	printf("Connection closed.\n");
	return (NULL);
}
/*
	Function used to select a joke from a predefined collection.
*/
char * makeUpJoke(char *first_name, char *last_name)
{
	char *buffer = malloc(strlen("Hello  ")+strlen(first_name)+strlen(last_name));
	sprintf(buffer,"Hello %s %s",first_name,last_name);
	return buffer;
}

void error(char *msg)
{
    perror(msg);
    exit(0);
}
