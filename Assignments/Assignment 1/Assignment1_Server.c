#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h> // Required to declare exit() function
#include <unistd.h> // Required to declare close() function
#include <stdio.h> // Required to declare perror() function
#include <string.h> // Required to declare memset(...) function

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

int setUpSocket();
void listenForRequests();
void error(char *msg);
char * makeUpJoke();

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

	serverSocketID = socket(PF_INET, SOCK_STREAM, 0);
	if (serverSocketID < 0) 
        error("ERROR opening socket");
	memset(&serverSocketAddr, 0, sizeof(struct sockaddr_in));
	serverSocketAddr.sin_family = AF_INET;
	serverSocketAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	serverSocketAddr.sin_port = htons(SERVER_PORT);
	if(bind(serverSocketID, (struct sockaddr *)&serverSocketAddr, sizeof(struct sockaddr_in)) < 0)
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
	int clientSocketID, rcvCount,sentCount,totalRcvd;
	char *first_name, *last_name, *joke;
	uint32_t addrLen = sizeof(struct sockaddr_in);
	struct sockaddr_in clientSocketAddr;
	joker_request jokerRequest;
	joker_response jokerResponse;
	uint8_t buffer_size;
	char *buffer;
	// Start listening on the socket created before.
	if(listen(serverSocketID, 10) < 0)
	{
		error("ERROR starting to listen");
	}
	printf("Server listening on port %d\n", SERVER_PORT);
	do {
		memset(&jokerRequest, 0, sizeof(jokerRequest)); // Fill the request's memory space with zeros.
		clientSocketID = accept(serverSocketID,(struct sockaddr *) &clientSocketAddr, &addrLen); // Accept an incoming request.
		printf("Connection accepted.\n");
		if((rcvCount = recv(clientSocketID, ((uint8_t *)&jokerRequest), sizeof(joker_request), 0)) < 0) // Read first the joke request's header
		{
			perror("ERROR receiving");
		}
		printf("Length of first name: %d\n", jokerRequest.len_first_name);
		printf("Length of last name: %d\n", jokerRequest.len_last_name);
		buffer_size = jokerRequest.len_first_name + jokerRequest.len_last_name;
		buffer = malloc(buffer_size); // Allocate some space to the buffer which will receive the first and last names
		first_name = malloc(jokerRequest.len_first_name + 1); // Allocate space to the first name according to the information received in the header.
		last_name = malloc(jokerRequest.len_last_name + 1); // Allocate space to the last name according to the information received in the header.
		do { // Receive from the socket as many characters as stated in the header.
			if((rcvCount = recv(clientSocketID, buffer + totalRcvd, buffer_size - totalRcvd, 0)) < 0)
			{
				error("ERROR receiving");
			}
			printf("Bytes received: %d\n",rcvCount);
			totalRcvd += rcvCount;
		} while(totalRcvd < buffer_size);
		// Once received the complete name, copy each of them to the corresponding variables and add a end-of-string character.
		memcpy(first_name,buffer,jokerRequest.len_first_name);
		memcpy(last_name,&buffer[strlen(first_name)],jokerRequest.len_last_name);
		first_name[jokerRequest.len_first_name] = '\0';
		last_name[jokerRequest.len_last_name] = '\0';
		printf("First name: %s \nLast name: %s\n",first_name,last_name);
		joke = makeUpJoke(); // Call the function in charge of creating a new joke and fill the header structure
		jokerResponse.type = JOKER_RESPONSE_TYPE;
		jokerResponse.len_joke = htonl(strlen(joke));
		if((sentCount = sendto(clientSocketID, &jokerResponse, sizeof(joker_response),0,  NULL, 0)) < 0)
    	{
    		error("ERROR sending");
    	}
    	printf("Characters sent: %d\n", sentCount); // Send the joke
    	if((sentCount = sendto(clientSocketID, joke, strlen(joke),0,  NULL, 0)) < 0)
    	{
    		error("ERROR sending");
    	}
    	printf("Characters sent: %d\n", sentCount);
    	// Free the resources and close the client's socket.
		free(first_name);
		free(last_name);
		free(buffer);
		close(clientSocketID);
		printf("Connection closed.\n");
	} while(1);
}
/*
	Function used to select a joke from a predefined collection.
*/
char * makeUpJoke()
{
	return "first joke";
}

void error(char *msg)
{
    perror(msg);
    exit(0);
}