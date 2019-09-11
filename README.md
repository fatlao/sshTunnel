# when your relay administrator forbid ssh tunneling.  
THIS MAY HELP

## usage

relay
1. download tun file from repo,put it at ${HOME} of relay
2. chmod +x tun  

local
1. install JDK1.8
2. download from repo: tool-sshTunnel-0.1beta.jar， tunnels.properties， jsch-0.1.53.jar，sshTunnel.bat
3. modify tunnels.properties
4. run sshTunnel.bat

## 

```prop
host=127.0.0.1           #ssh address of relay
port=22                  #ssh port
username=${smmoth_criminal}        # username
password=${annie_are_you_ok}       # password
commandline=/path/to/tun 

30193=10.1.18.193 32200         #localPort=remoteAddress remotePort
30198=10.1.0.198 32200
```
