/* 

	Communication Systems Lab
	Assignment 1
	Topic: Socket Programming
	TCP Client implementation

*/
#include <stdio.h> // Required to declare perror() function
#include <stdlib.h> // Required to declare exit() function
#include <unistd.h> // Required to declare close() function
#include <stdint.h>
#include <string.h> // Required to declare memset(...) function
#include <sys/time.h>

// Headers required for working with sockets
#include <netinet/in.h>
#include <arpa/inet.h>

#define JOKER_REQUEST_TYPE 1
#define JOKER_RESPONSE_TYPE 2

typedef struct {
	uint8_t type;
} __attribute__ ((__packed__)) joker_header;
     
typedef struct {
	uint8_t type;
	uint8_t len_first_name;
	uint8_t len_last_name;
} __attribute__ ((__packed__)) joker_request;

typedef struct {
	uint8_t type;
	uint32_t len_joke;
} __attribute__ ((__packed__)) joker_response;

char ipAddress[15];
uint16_t portNumber;
char firstName[255];
char lastName[255];
joker_request jokerRequest;
joker_response jokerResponse;
char *incomingJoke;

void obtainUserInput();
int setUpSocket();
void error(char *msg);
/*
	Main function which handles the communication with the server.
*/
int main()
{
	int socketId;
	int sentCount;
	int rcvCount;
	unsigned long totalRcvd = 0; // Number of total bytes received
	unsigned long jokeLengthHost = 0;
	obtainUserInput();
	socketId = setUpSocket();
	memset(&jokerResponse, 0, sizeof(joker_response)); // Fill the response's memory space with zeros.
    if((sentCount = sendto(socketId, (&jokerRequest), sizeof(joker_request),0,  NULL, 0)) < 0) // Send joke request header
    {
    	error("ERROR sending");
    }
    printf("Characters sent: %d\n", sentCount);
    if((sentCount = sendto(socketId, firstName, strlen(firstName),0,  NULL, 0)) < 0) // Send first name and last name separately
    {
    	error("ERROR sending");
    }
    printf("Characters sent: %d\n", sentCount);
    if((sentCount = sendto(socketId, lastName, strlen(lastName),0,  NULL, 0)) < 0)
    {
    	error("ERROR sending");
    }
    printf("Characters sent: %d\n", sentCount);
    do{
		if((rcvCount = recv(socketId, ((uint8_t *)&jokerResponse) + totalRcvd, sizeof(joker_response) - totalRcvd, 0)) < 0) // Receive response header
		{
			error("ERROR receiving");
		}
		totalRcvd += rcvCount;
		printf("Bytes received: %d\n",rcvCount);
	}while(totalRcvd < sizeof(joker_response));
	totalRcvd = 0;
	jokeLengthHost = ntohl(jokerResponse.len_joke); // Calculate joke length considering the message representation
	printf("Joke length: %lu\n", jokeLengthHost);
	incomingJoke = malloc(jokeLengthHost + 1);
    
    do {
		if((rcvCount = recv(socketId, incomingJoke + totalRcvd, jokeLengthHost - totalRcvd, 0)) < 0) // Receive joke
		{
			error("ERROR receiving");
		}
		printf("Bytes received: %d\n",rcvCount);
		totalRcvd += rcvCount;
	} while(totalRcvd < jokeLengthHost);
	close(socketId);
	incomingJoke[jokeLengthHost] = '\0'; // Attach end-of-string character to the joke
	printf("Message received: \n\n\t%s\n", incomingJoke);
	free(incomingJoke);
	return 0;
}
/*
	Function used to initialize the socket using IP protocol, TCP method and a destination IP Address/port.
*/
int setUpSocket()
{
	struct sockaddr_in serv_addr;
	memset(&serv_addr, 0, sizeof(struct sockaddr_in)); 
	serv_addr.sin_family = AF_INET; // Select Internet address family
	serv_addr.sin_port = htons(portNumber); // Set the port following the network's presentation
	inet_aton(ipAddress,(struct in_addr *)&serv_addr.sin_addr); // Assign IP Address in the corresponding representation
	int socketId = socket(PF_INET,SOCK_STREAM,0); // Create TCP socket
	if (socketId < 0) 
        error("ERROR opening socket");
    struct timeval tv;
	tv.tv_sec = 3;  /* 3 Secs Timeout */
    setsockopt(socketId, SOL_SOCKET, SO_RCVTIMEO,(char *)&tv, sizeof(struct timeval)); // Assign 3 seconds timeout to socket
	if (connect(socketId,(struct sockaddr *)&serv_addr, sizeof(struct sockaddr_in)) < 0) // Establish connection using the IP address
        error("ERROR connecting");
    return socketId;
}
/*
	Function used to gather information from the user input
*/
void obtainUserInput()
{
	printf("Specify IP address\n");
	scanf("%s", ipAddress);
	printf("Specify a port number\n");
	scanf("%hu", &portNumber);
	printf("You entered: %s:%d\n", ipAddress,portNumber);

	printf("Tell me your first name:\n");
	scanf("%s", firstName);
	printf("Tell me your last name:\n");
	scanf("%s", lastName);
	printf("You entered: %s %s\n", firstName,lastName);

	jokerRequest.type = JOKER_REQUEST_TYPE;
	jokerRequest.len_first_name = strlen(firstName);
	jokerRequest.len_last_name = strlen(lastName);

}

void error(char *msg)
{
    perror(msg);
    exit(0);
}
