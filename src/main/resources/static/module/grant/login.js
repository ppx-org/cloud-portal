$(function () {
	var f = '<div style="margin-top:50px">\
	<form class="bs-example bs-example-form" role="form" style="width:500px;padding-left:80px;margin: 0 auto;">\
		<div class="input-group" style="width:500px">\
			<span class="input-group-addon"><span class="glyphicon glyphicon-user"></span></span>\
			<input type="text" class="form-control" placeholder="请输入您的账号" style="width:300px"\
				id="a" data-toggle="popover" data-content="请输入您的账号！" data-placement="right" data-trigger="manual" value="admin">\
		</div>\
		<div class="input-group" style="width:500px;margin-top:10px">\
			<span class="input-group-addon"><span class="glyphicon glyphicon-lock"></span></span>\
			<input type="password" class="form-control" placeholder="请输入您的密码" style="width:300px"\
				id="p" data-toggle="popover" data-content="请输入您的密码！" data-placement="right" data-trigger="manual" value="123">\
		</div>\
		<div class="input-group" style="width:500px;margin-top:10px" id="loginBtnDiv">\
			<button type="button" class="btn btn-primary popover-show"  style="width:340px" onclick="login()"\
				id="b" data-toggle="popover" data-content="账号或密码错误！" data-placement="right" data-trigger="manual">登录\
			</button>\
		</div>\
	</form>\
	</div>';
	$("body").html(f);
		
	$("#a").focus(function(){
		$('#a').popover('hide');
		$('#b').popover('hide');
	});
	
	$("#p").focus(function(){
		$('#p').popover('hide');
		$('#b').popover('hide');
	});
		
});


function login() {
	var a = $.trim($('#a').val());
	var p = $.trim($('#p').val());
	if (a == "") {
		$('#a').popover('show');
		return;
	}
	if (p == "") {
		$('#p').popover('show');
		return;
	}
	
	$("#b").attr("disabled", true);
	$("#b").text("登录中...");
	
	var v = function() {
		var c = $('meta[id="DhefwqGPrzGxEp9hPaoag"]').attr("content");
		c = c.substring(c.length - 21);
		var n = "";
		for (i in c) {
			n += (c.charCodeAt(i) + "DhefwqGPrzGxEp9hPaoag".charCodeAt(i)) + "";
		}
		return n;
	}
 
	$.post(contextPath + "login/doLogin", {a:a,p:p,v:v()}, function(r){
		if (r.code == -1) {
			$('#b').popover('show');
			$("#b").attr("disabled", false);
			$("#b").text("登录");
		}
		else if (r.code == 1) {
			location.href = contextPath + "grant/service";
		}
		else if (r.code == 2) {
			location.href = "demo/test/listTest";
		}
	});	
}
