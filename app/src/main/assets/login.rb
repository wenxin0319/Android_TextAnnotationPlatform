#encoding: utf-8
#输入参数 $username, $password，顾名思义

#puts("Login #{$username} #{$password}")

require 'socket'

#STEP1: get login page
login_page = [
"GET /login HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"\r\n"
].join("\r\n")
request_do(login_page) {|res|
    parse_cookie(res)    
    res.each_line {|line|
        if line.include? "csrf_token"
            /value="(.+)"/.match(line)
            $csrf_token = $1
        end
    }
}

#STEP2: login and redirect to /index
login_req_head = [
"POST /login HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"Cookie: session=#{get_session}",
"Content-Type: application/x-www-form-urlencoded",
"Content-Length: %d",
"\r\n"
].join("\r\n")
login_req_content = "username=#{$username}&password=#{$password}&submit=%%E7%%99%%BB%%E5%%BD%%95&csrf_token=%s"
login_req_content = sprintf(login_req_content, $csrf_token)
login_req = sprintf(login_req_head, login_req_content.length) + login_req_content
request_do(login_req) {|res|
    if res.lines.to_a[0] =~ /302 FOUND/
        parse_cookie(res)
    else
        raise Exception, "username=#{$username}, password=#{$password}, login failed"
    end
}
index_req = [
"GET /index HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")
request_do(index_req) {|res|
    parse_cookie(res)
}
