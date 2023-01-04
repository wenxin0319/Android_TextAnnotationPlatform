#选择迭代的结果类型和实体类型
#传入参数 $iterate_result_type = -1 全部
#                                0  智能标注
#                                1  实体标注
#        $iterate_entity_type = "全部" 全部
#                               其它标签
#返回 $return=满足条件的标签的json
require 'socket'

if $iterate_result_type == -1
    $iterate_result_type = [34, 229, 133, 168, 233, 131, 168, 34].pack("C*")
else
    $iterate_result_type = $iterate_result_type.to_s.inspect
end

result_type_req = [
"POST /result_type_select HTTP/1.0",
"Host: #{$host}",
"Content-Length: #{$iterate_result_type.bytes.to_a.length}",
"Accept: */*",
"Origin: http://#{$host}",
"X-Requested-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Referer: http://#{$host}/admin_iter",
"Accept-Encoding: gzip, deflate, br",
"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8,en;q=0.7",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")

puts("before request result type")
request_do(result_type_req + $iterate_result_type) {|res|
    parse_cookie(res)
}


$iterate_entity_type = $iterate_entity_type.inspect
entity_type_req = [
"POST /entity_type_select HTTP/1.0",
"Host: #{$host}",
"Content-Length: #{$iterate_entity_type.bytes.to_a.length}",
"Accept: */*",
"Origin: http://#{$host}",
"X-Requested-With: XMLHttpRequest",
"Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
"Referer: http://#{$host}/admin_iter",
"Accept-Encoding: gzip, deflate, br",
"Accept-Language: zh-CN,zh;q=0.9,und;q=0.8,en;q=0.7",
"Cookie: session=#{get_session}",
"\r\n"].join("\r\n")

request_do(entity_type_req + $iterate_entity_type) {|res|
    parse_cookie(res)
}

iterate_entity_browse_req = [
"GET /entity_browse HTTP/1.0",
"Host: #{$host}",
"Accept: application/json, text/javascript, */*; q=0.01",
"X-Requested-With: XMLHttpRequest",
"Referer: http://#{$host}/admin_iter",
"Cookie: session=#{get_session}",
"\r\n"].join("\r\n")

request_do(iterate_entity_browse_req) {|res|
    parse_cookie(res)
    $return = get_http_content(res)
}