# when your relay administrator forbid ssh tunneling.  
THIS MAY HELP

## function
1.forward your tcp streams without listening any ports on relay.  
2.binding remote port on localhost.  

## how does it works
Transfer your tcp I/O streams into terminal's std I/0 stream with HEX encoding.

** why not transfer with z-modem protocol? **   
My company's operation administrator forbids this protocol.

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

## hint
kg 只在跳板机上面禁止了tunnel.  
通过这个跳到业务机再使用原生ssh隧道可以直连接数据库做爱做的事.    
然后跑路
