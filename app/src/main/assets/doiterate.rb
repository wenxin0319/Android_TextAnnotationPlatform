# doiterate.rb
#   输入参数：$iterate_data

do_iterate_req = [
"POST /confirm_iter HTTP/1.0",
"Host: #{$host}",
"Content-Length: #{$iterate_data.bytes.to_a.length}",
"Accept: */*",
"X-Requested-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Referer: http://#{$host}/admin_iter",
"Accept-Encoding: gzip, deflate, br",
"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8,en;q=0.7",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")

request_do(do_iterate_req + $iterate_data) { |res|
    parse_cookie(res)
    $return = get_http_content(res)
}