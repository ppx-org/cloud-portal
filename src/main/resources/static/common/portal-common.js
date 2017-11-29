
$(function(){
	// 增加排序图标
	$("th[data-order-name]").each(function(){
		if (!$(this).hasClass("page-sorting-asc") && !$(this).hasClass("page-sorting-desc")) {
			$(this).addClass('page-sorting');
		}
		$(this).append('<span class="glyphicon"></span>');		
	});
	
	var loading = '<div class="modal fade" id="loading" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" data-backdrop="static">\
		<div class="modal-dialog" role="document"><div class="modal-content"><div style="padding-top:13px;">\
		<span class="glyphicon glyphicon-info-sign"></span>请稍候...</div></div></div></div>';
	$('body').append(loading);
	
	$('body').append('<div id="myAlert"><strong id="myAlertMsg"></strong></div>');
	
	
	var confirm = '<div class="modal fade" id="myConfig"><div class="modal-dialog"><div class="modal-content"><div class="modal-header">\
		<button aria-hidden="true" class="close" data-dismiss="modal" type="button">×</button><h5 class="modal-title">确认</h5></div>\
		<div class="modal-body"><div class="form-group" style="text-align: center;margin-bottom:-5px"><span id="myConfigMsg"></span></div></div>\
		<div class="modal-footer"><button class="btn btn-primary btn-sm" onclick="myConfigOk()" type="button">确定</button>\
		<button class="btn btn-default btn-sm" data-dismiss="modal" type="button">关闭</button></div></div></div></div>';
	$('body').append(confirm);
	
});

function showLoading() {
	$('#loading').modal('show');
	// 为了在modal页也能显示loading
	$($(".modal-backdrop")[$(".modal-backdrop").length - 1]).css("z-index", "9999");
	
	$("#loading .modal-content").hide();
	$($(".modal-backdrop")[$(".modal-backdrop").length - 1]).css("opacity", "0");
	// n毫秒没有加载完页面才出现loading
	$("#loading").data("isShowLoading", true);
	setTimeout(function() {
		if ($("#loading").data("isShowLoading")) {
			$("#loading .modal-content").show();
			$(".modal-backdrop").css("opacity", "0.5");
		}
	}, 300);
}

function hideLoading() {
	$("#loading").data("isShowLoading", false);
	$('#loading').modal('hide');
}

$.ajaxSetup({
	error: function(r, textStatus, errorThrown) {
		$('#loading').modal('hide');
		alertDanger("error:" + r.status + "|" + this.url);
	}
});

function alertSuccess(msg) {alertShow(msg, "alert-success", 1200)}
function alertInfo(msg) {alertShow(msg, "alert-info", 1200)}
function alertWarning(msg) {alertShow(msg, "alert-warning", 1200)}
function alertDanger(msg) {alertShow(msg, "alert-danger", 2000)}

function confirm(msg, func) {
	$("#myConfig").data("func", func);
	$("#myConfigMsg").text(msg);
	$("#myConfig").modal('show');
}

function myConfigOk() {
	$("#myConfig").data("func").call();
	$("#myConfig").modal('hide');
}

function alertShow(msg, cls, time) {
	$("#myAlertMsg").text(msg);
	$("#myAlert").attr("class", "alert " + cls);
	$("#myAlert").show();
	setTimeout('$("#myAlert").hide();', time);
}

// 分页begin >>>>>>
function activeSorting() {
	$(".page-sorting, .page-sorting-asc, .page-sorting-desc").unbind("click");
	$(".page-sorting, .page-sorting-asc, .page-sorting-desc").click(function(){
		var orderName = $(this).attr("data-order-name");		
		if ($(this).hasClass("page-sorting-desc")) {
			$("#orderName").val(orderName);
			$("#orderType").val("asc");
			$(".page-sorting-asc, .page-sorting-desc").addClass("page-sorting");
			$(".page-sorting-asc").removeClass("page-sorting-asc");
			$(".page-sorting-desc").removeClass("page-sorting-desc");
			$(this).addClass("page-sorting-asc");
		} else {
			$("#orderName").val(orderName);
			$("#orderType").val("desc");
			$(".page-sorting-asc, .page-sorting-desc").addClass("page-sorting");
			$(".page-sorting-asc").removeClass("page-sorting-asc");
			$(".page-sorting-desc").removeClass("page-sorting-desc");
			$(this).addClass("page-sorting-desc");
		}
		queryPage(1);
	});
}

function refreshPageData(data, pageTemplateId) {
	if (!data) return;
	if (!pageTemplateId) pageTemplateId = "pageTemplate";
	
	$("#" + pageTemplateId).parent().find("tr:gt(0)").remove();
	$("#" + pageTemplateId).parent().append(template(pageTemplateId, data));
	if (data.page) refreshFooter(data.page);
	if (data.springDataPageable) refreshFooter(data.springDataPageable);
}

function refreshFooter(p) {
	activeSorting();
	
	// p.pageSize每页几个记录 p.totalRows总记录数 p.pageNumber当前页 
	$("#totalRows").text(p.totalRows);
	var pageTotalNum = Math.ceil(p.totalRows/p.pageSize);
	$("#pageNumUL").empty();
	if (p.pageNumber == 1) {
		$("#pageNumUL").append('<li class="disabled"><a>«</a></li>');
	}
	else {
		$("#pageNumUL").append('<li><a href="#this" onclick="queryPage(1)">«</a></li>');
	}
	
	var begin = p.pageNumber <= 3 ? 1 : p.pageNumber - 2;
	for (var i = begin; i < begin + 5 && i <= pageTotalNum; i++) {
		var activeClass = "";
		if (i == p.pageNumber) activeClass = 'class="active"';
		$("#pageNumUL").append('<li ' + activeClass + '><a href="#this" onclick="queryPage(' + i + ')">' + i + '</a></li>');
	}
	
	if (p.totalRows == 0 || p.pageNumber == pageTotalNum) {
		$("#pageNumUL").append('<li class="disabled"><a href="#this">»</a></li>')
	}
	else {
		$("#pageNumUL").append('<li><a href="#this" onclick="queryPage(' + pageTotalNum + ')">»</a></li>');
	}
}
//分页end >>>>>>







