#encoding: utf-8
#输入参数： $tasks_page: 任务页
#         $taskset 任务集(id)
#传出任务列表参数json

=begin
get_mark_page_req = [
"GET /mark?page=#{$page} HTTP/1.0",
"Host: #{$host}",
"Cache-Control: max-age=0",
"\r\n"
].join("\r\n")
=end

select_task_data = <<JSOND
{"data":{"task_id":"#{$taskset}"}}
JSOND

#puts(select_task_data)


select_taskset_req = [
"POST /select_task HTTP/1.0",
"Host: #{$host}",
"Accept: */*",
"X-Requested-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Referer: http://#{$host}/mark?page=#{$page}",
"Content-Length: #{select_task_data.length}",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")

sample_list_req = [
"GET /sample_list HTTP/1.0",
"Accept: application/json, text/javascript, */*; q=0.01",
"Referer: http://#{$host}/mark?page=#{$page}",
"X-Requested-With: XMLHttpRequest",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")


request_do(select_taskset_req + select_task_data) { |res|
    parse_cookie(res)
}

request_do(sample_list_req) {|res|
    #print(get_http_content res)
    parse_cookie(res)
    $return = (get_http_content res)
}
