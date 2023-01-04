# 领取新样本 按钮 的响应
# 输入参数 $taskset 任务集id

json_data = <<JSOND
{"task_id":"#{$taskset}"}
JSOND

req = [
"POST /obtain_new_sample HTTP/1.0",
"Host: #{$host}",
"Content-Length: #{json_data.length}",
"Accept: */*",
"Origin: http://#{$host}",
"X-Requested-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Sec-Fetch-Site: same-origin",
"Referer: http://#{$host}/mark?page=#{$page}",
"Accept-Encoding: gzip, deflate, br",
"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8,en;q=0.7",
"\r\n"
].join("\r\n")

request_do(req + json_data) {|res|
    parse_cookie(res)
}