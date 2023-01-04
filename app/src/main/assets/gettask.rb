#encoding: utf-8
#传入参数 $selected_task_json，被选中的任务的json
#传出返回的json
#require 'json'

get_mark_req = [
"POST /mark HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"Content-Length: #{$selected_task_json.bytes.to_a.length}",
"X-Request-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Origin: http://#{$host}",
"Referer: http://#{$host}/mark",
#"Accept-Encoding: gzip, deflate, br",
#"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8en;q=0.7",
"Cookie: session=#{get_session}",
"\r\n",
].join("\r\n")

request_do(get_mark_req + $selected_task_json) {|res|
    line0 = res.lines.to_a[0]
    if line0 =~ /200 OK/ || line0 =~ /302 FOUND/
        parse_cookie(res)
        #$return = JSON.parse(get_http_content res)
        $return = get_http_content res
    else
        raise Exception, "Get task failed"
    end
}
