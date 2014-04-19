/* 

	Communication Systems Lab
	Assignment 1
	Topic: Socket Programming

*/
#include <stdio.h>
#include <stdlib.h> // Required to declare exit() function
#include <unistd.h> // Required to declare close() function
#include <stdint.h>
#include <string.h>
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
	char *first_name;
	char *last_name;
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

int main()
{
	int socketId;
	int sentCount;
	int rcvCount;
	int totalRcvd = 0; // Number of total bytes received
	int jokeLengthHost = 0;
	obtainUserInput();
	socketId = setUpSocket();
	memset(&jokerResponse, 0, sizeof(joker_response));
    if((sentCount = sendto(socketId, (&jokerRequest), sizeof(joker_request),0,  NULL, 0)) < 0)
    {
    	error("ERROR sending");
    }
    printf("Characters sent: %d\n", sentCount);
    do{
		if((rcvCount = recv(socketId, ((uint8_t *)&jokerResponse) + totalRcvd, sizeof(joker_response) - totalRcvd, 0)) < 0)
		{
			error("ERROR receiving");
		}
		totalRcvd += rcvCount;
		printf("Bytes received: %d\n",rcvCount);
	}while(totalRcvd < sizeof(joker_response));
	totalRcvd = 0;
	jokeLengthHost = ntohl(jokerResponse.len_joke);
	printf("Joke length: %u\n", jokeLengthHost);
	incomingJoke = malloc(jokeLengthHost + 1);
    
    do {
		if((rcvCount = recv(socketId, incomingJoke + totalRcvd, jokeLengthHost - totalRcvd, 0)) < 0)
		{
			error("ERROR receiving");
		}
		printf("Bytes received: %d\n",rcvCount);
		totalRcvd += rcvCount;
	} while(totalRcvd < jokeLengthHost);
	close(socketId);
	incomingJoke[jokeLengthHost] = '\0';
	printf("Message received: \n\n\t%s\n", incomingJoke);
	free(incomingJoke);
	return 0;
}

int setUpSocket()
{
	struct sockaddr_in serv_addr;
	memset(&serv_addr, 0, sizeof(struct sockaddr_in)); 
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(portNumber);
	inet_aton(ipAddress,(struct in_addr *)&serv_addr.sin_addr);
	int socketId = socket(PF_INET,SOCK_STREAM,0);
	if (socketId < 0) 
        error("ERROR opening socket");
    struct timeval tv;
	tv.tv_sec = 3;  /* 3 Secs Timeout */
    setsockopt(socketId, SOL_SOCKET, SO_RCVTIMEO,(char *)&tv, sizeof(struct timeval));
	if (connect(socketId,(struct sockaddr *)&serv_addr, sizeof(struct sockaddr_in)) < 0) 
        error("ERROR connecting");
    return socketId;
}

void obtainUserInput()
{
	printf("Specify IP address\n");
	scanf("%s", ipAddress);
	printf("Specify a port number\n");
	scanf("%hu", &portNumber);
	printf("You entered: %s:%d\n", ipAddress,portNumber);

	printf("Tell me your first name:\n");
	scanf("%s", &firstName);
	printf("Tell me your last name:\n");
	scanf("%s", &lastName);
	printf("You entered: %s %s\n", firstName,lastName);

	jokerRequest.type = JOKER_REQUEST_TYPE;
	jokerRequest.len_first_name = strlen(firstName);
	jokerRequest.len_last_name = strlen(lastName);
	jokerRequest.first_name = firstName;
	jokerRequest.last_name = lastName;

}

void error(char *msg)
{
    perror(msg);
    exit(0);
}