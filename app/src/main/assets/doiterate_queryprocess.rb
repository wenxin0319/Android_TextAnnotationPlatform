#查询迭代的进度

do_iterate_query_req = [
"POST /mes_percentage_now HTTP/1.0",
"Host: #{$host}",
"Accept: */*",
"X-Requested-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Referer: http://#{$host}/admin_iter",
"Accept-Encoding: gzip, deflate, br",
"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8,en;q=0.7",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")

request_do(do_iterate_query_req) { |res|
    parse_cookie(res)
    content = get_http_content(res)
    if content
        $return = content.gsub('"', '')
    else
        $return = "0"
    end
}