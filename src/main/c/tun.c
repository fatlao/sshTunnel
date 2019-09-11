#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/shm.h>
#include <pthread.h>

#define MYPORT 4444
#define BUFFER_SIZE 1024

char hextbl[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

void doUpstream(int* sock_cli){

    char sendbuf[BUFFER_SIZE];
    char * stop;


    while ( fgets(sendbuf, sizeof(sendbuf), stdin ) != NULL){

        long int data = strtol (sendbuf,&stop,16);

        sendbuf[0]=data;

        int bytesend = send(*sock_cli,sendbuf,sizeof(char),MSG_WAITALL);

        if(bytesend <= 0){
              return;
        }

    }

}



void doDownstream(int* sock_cli){

    unsigned char recvbuf[1];
    char * stop;

    char output[BUFFER_SIZE];



    while ( recv(*sock_cli, recvbuf, sizeof(recvbuf),MSG_WAITALL)>0 ){
        output[0]=hextbl[(recvbuf[0]>>4)];
        output[1]=hextbl[(recvbuf[0])&0x0Fu];
        output[2]='\n';
        output[3]=0;




        fputs(output,stdout);
        fflush(stdout);

    }

}

void check(){

    while(1){
        sleep(1);
        char line[1024];
        FILE *fp;
        fp = fopen("/proc/self/status","rt");
        if(fp == NULL){
            exit(1);
        }

        while(1){
            if(fgets(line,100,fp) != NULL){


                char * pidnum = strstr(line,"PPid:\t");

                if(!pidnum){
                    continue;
                }
                //printf("%s",&pidnum[6]);

                if(pidnum[6] == '1' && pidnum[7]=='\n'){
                    exit(1);
                }else{
                    continue;
                }

            }else{
            break;
            }
        }

        fclose(fp);
    }


}

int main(int argc,char * argv[]){
    int sock_cli = socket(AF_INET,SOCK_STREAM, 0);
    struct sockaddr_in servaddr;
    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    int port = atoi(argv[2]);


    servaddr.sin_port = htons(port);
    servaddr.sin_addr.s_addr = inet_addr(argv[1]);
    if (connect(sock_cli, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        perror("connect");
        exit(1);
    }


    pthread_t send_t,recv_t,check_t;

    pthread_create(&send_t,NULL,(void *)doUpstream,&sock_cli);
    pthread_create(&recv_t,NULL,(void *)doDownstream,&sock_cli);
    pthread_create(&check_t,NULL,(void *)check,NULL);


    pthread_join(send_t,NULL);
    pthread_join(recv_t,NULL);
    pthread_join(check_t,NULL);


    close(sock_cli);

    return 0;


}

