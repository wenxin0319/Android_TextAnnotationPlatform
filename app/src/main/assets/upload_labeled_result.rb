#输入参数 $upload_json

puts($upload_json)

upload_labeled_req = [
"POST /mark_submit_1 HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"Content-Length: #{$upload_json.bytes.to_a.length}",
"X-Request-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Origin: http://#{$host}",
"Referer: http://#{$host}/mark",
"Accept-Encoding: gzip, deflate, br",
"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8en;q=0.7",
"Cookie: session=#{get_session}",
"\r\n",
].join("\r\n")

request_do(upload_labeled_req + $upload_json) {|res|
    if res.lines.to_a[0] =~ /200 OK/
        parse_cookie(res)
    else
        raise "Upload failed"
    end
}