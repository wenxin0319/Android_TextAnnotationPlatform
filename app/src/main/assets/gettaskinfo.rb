get_task_info_req = [
"GET /load_task_all_data HTTP/1.0",
"Host: #{$host}",
"Accept: application/json, text/javascript, */*; q=0.01",
"X-Requested-With: XMLHttpRequest",
"Referer: http://#{$host}/task_manage?page=1",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")

request_do(get_task_info_req) {|res|
    parse_cookie(res)
    $return = get_http_content res
}