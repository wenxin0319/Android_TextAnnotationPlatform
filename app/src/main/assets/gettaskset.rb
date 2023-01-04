#encoding: utf-8
#获得任务集

get_req = [
"GET /task_select_in_sample_labeling HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"X-Request-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Origin: http://#{$host}",
"Referer: http://#{$host}/mark",
"Cookie: session=#{get_session}",
"\r\n",
].join("\r\n")

request_do(get_req) {|res|
    line0 = res.lines.to_a[0]
    if line0 =~ /200 OK/ || line0 =~ /302 FOUND/
        parse_cookie(res)
        $return = get_http_content res
    else
        raise Exception, "Get task failed"
    end
}
