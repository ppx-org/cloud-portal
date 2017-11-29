var move = {};
move.toOrder = function(a) {
	showLoading();
	$.post(contextPath + 'grant/orderService', "serviceIds=" + a, function(r) {
		freshPage(r);
		hideLoading();
	});
}
move.toTop = function(serviceId) {
	var a = [serviceId];
	$("[name=serviceId]").each(function(){
		if (serviceId != $(this).text()) {
			a.push($(this).text());
		}
	});
	this.toOrder(a);
}
move.toUp = function(n) {
	var a = [];
	$("[name=serviceId]").each(function(){		
		a.push($(this).text());		
	});
	var upServiceId = a[n];
	var targetUpServiceId = a[n - 1];
	// 交换
	a[n] = targetUpServiceId;
	a[n - 1] = upServiceId;
	this.toOrder(a);
}
move.toDown = function(n) {
	var a = [];
	$("[name=serviceId]").each(function(){		
		a.push($(this).text());		
	});
	var downServiceId = a[n];
	var targetDownServiceId = a[n + 1];
	// 交换
	a[n] = targetDownServiceId;
	a[n + 1] = downServiceId;
	this.toOrder(a);
}

function freshPage(data) {
	if (!data) return;
	
	$("#pageTemplate").parent().find("tr:gt(0)").remove();
	$("#pageTemplate").parent().append(template("pageTemplate", data));
}

function query() {
	$.post(contextPath + 'grant/listService', null, function(r) {
		freshPage(r);
		hideLoading();
	});
}


function addOk() {
	if ($("#addServiceName").val() == "") {
		alertWarning("微服务名称不能为空！");
		return;
	}
	if ($("#addContextPath").val() == "") {
		alertWarning("context-path不能为空！");
		return;
	}
		
	showLoading();
	$.post(contextPath + 'grant/addService', $("#addForm").serialize(), function(r){		
		$("#add").modal("hide");
		alertSuccess("新增成功！");
		query();
	});
}

function edit(_id, serviceName, contextPath) {
	$("#updateId").val(_id);
	$("#updateServiceName").val(serviceName);
	$("#updateContextPath").val(contextPath);	
	$("#edit").modal("show");
}

function editOk() {
	if ($("#updateServiceName").val() == "") {
		alertWarning("微服务名称不能为空！");
		return;
	}
	if ($("#updateContextPath").val() == "") {
		alertWarning("context-path不能为空！");
		return;
	}
	
	showLoading();
	$.post(contextPath + 'grant/updateService', $("#editForm").serialize(), function(r) {
		$("#edit").modal("hide");
		alertSuccess("修改成功！");
		query();
	});
}

function remove(_id) {
	var callback = function() {
		showLoading();
		$.post(contextPath + 'grant/removeService', "_id=" + _id, function(r) {
			if (r.result == -1) {
				hideLoading();
				alertWarning("存在资源，删除失败！");
			}
			else {
				alertSuccess("删除成功！");
				query();
			}
		});
	}
	confirm("确定要删除" + _id + "?", callback);
}

