#encoding: utf-8
#输入参数: $host(ip+域名)

require 'socket'

$host_name = $host.split(':')[0]
$host_port = $host.split(':')[1].to_i

def request_do(data, &block)
    s = TCPSocket.new($host_name, $host_port)
    s.print data
    res = s.read
    s.close
    block.call(res) if block
end

$cookies = {}

def parse_cookie(res)
    res.each_line {|line|
        if /Set\-Cookie:(.+)/.match(line)
            $1.split(';').each {|cookie|
                kv = cookie.split('=')
                $cookies[kv[0]] = kv[1]
            }
        end
    }
end

def get_session
    $cookies[" session"] || ""
end

def get_http_header(res)
    res.split("\r\n\r\n")[0]
end

def get_http_content(res)
    res.split("\r\n\r\n")[1]
end