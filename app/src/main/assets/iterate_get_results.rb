iterate_get_result_req = [
"GET /entity_sample_info HTTP/1.0",
"Host: #{$host}",
"Accept: application/json, text/javascript, */*; q=0.01",
"X-Requested-With: XMLHttpRequest",
"Referer: http://#{$host}/admin_iter",
"Cookie: session=#{get_session}",
"\r\n"
].join("\r\n")

request_do(iterate_get_result_req) {|res|
    parse_cookie(res)
    $return = get_http_content(res)
}