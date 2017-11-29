
var treeUtils = {childrenId:[]};
treeUtils.getChildrenIds = function(node) {
	this.childrenId.push(node.id);
	if (node.nodes) {
		for (var i = 0; i < node.nodes.length; i++) {
			this.getChildrenIds(node.nodes[i]);
		}
	}
	return this.childrenId;
}
treeUtils.getNodeIcon = function(nodeType) {
	// -1资源  0目录 1菜单 2操作
	if (nodeType == 0) return "glyphicon glyphicon-folder-close";
	if (nodeType == 1) return "glyphicon glyphicon-th-list";
	if (nodeType == 2) return "glyphicon glyphicon-wrench";
	return "glyphicon glyphicon-home";
}
treeUtils.getNodeType = function(nodeIcon) {
	if (nodeIcon == "glyphicon glyphicon-folder-close") return 0;
	if (nodeIcon == "glyphicon glyphicon-th-list") return 1;
	if (nodeIcon == "glyphicon glyphicon-wrench") return 2;
	return -1;
}
treeUtils.compressNode = function(node) {
	var newNode = {id:node.id,t:node.text,i:this.getNodeType(node.icon)};
	if (node.nodes) {
		newNode.n = [];
		for (var i = 0; i < node.nodes.length; i++) {
			newNode.n.push(this.compressNode(node.nodes[i]));
		}
	}
	return newNode;
}
treeUtils.decompressNode = function(node) {
	var newNode = {id:node.id,text:node.t,icon:this.getNodeIcon(node.i)}
	if (node.n) {
		newNode.nodes = [];
		for (var i = 0; i < node.n.length; i++) {
			newNode.nodes.push(this.decompressNode(node.n[i]));
		}
	}
	return newNode;
}


function initTree(tree) {
	$('#tree').treeview({data:tree,levels:2,
		onNodeSelected:function(event, data) {			
			var nodeType = treeUtils.getNodeType(data.icon);
			if (nodeType == -1) {
				$("#addChildId").show();
				$(".operatorNode").hide();				
			}
			else if (nodeType == 2) {
				$("#addChildId").hide();
				$(".operatorNode").show();
			}
			else {
				$("#addChildId").show();
				$(".operatorNode").show();
			}
			
			move.refreshMoveButton();
			var top = $('#tree').find("[data-nodeid='" + data.nodeId + "']").position().top - 50;
			$("#uri").css("margin-top", top);
						
			// 读取uri
			$("#uriList li:gt(0)").remove();
			if (data.id) {
				$("#uriList li:gt(0)").remove();
				$("#uriList").append('<li class="list-group-item" style="background-color: white;"><span>读取中...</span></li>');
				$.post(contextPath + 'grant/getUri', "resId=" + data.id, function(r){					
					$("#uriList li:gt(0)").remove();
					var uriList = [];
					for (var i = 0; r.uri && i < r.uri.length; i++) {
						uriList.push({uri:r.uri[i], uriIndex:r.uriIndex[i]});
					}
					$("#uriList").append(template('uriListTemplate', uriList));
				});
			}
			
			$("#uri").show();			
		},
		onNodeUnselected:function(event, data) {
			$("#uri").hide();
		},
		onNodeCollapsed:function(event, data) {
			var node = $('#tree').treeview('getSelected');			
			$('#tree').treeview('unselectNode', [node,{silent:true}]);
			$("#uri").hide();
		},
		onNodeExpanded:function(event, data) {
			var node = $('#tree').treeview('getSelected');			
			$('#tree').treeview('unselectNode', [node,{silent:true}]);
			$("#uri").hide();
		}
	});
}

function saveResource(removeIds) {
	var tree = JSON.stringify(treeUtils.compressNode($('#tree').treeview('getNode', 0)));	
	showLoading();
	var para = "serviceId=" + currentServiceId + "&tree=" + tree + "&removeIds=" + removeIds;
	$.post(contextPath + 'grant/saveResource', para, function(r){
		hideLoading();
		alertSuccess("保存成功！");	
	});
}

var currentServiceId;
var typeHeadSource;
var typeHeadSourceMap = [];
function onService(obj, _id, servicePath) {
	currentServiceId = _id;
	typeHeadSource = ['/加载中...稍后请重新打开'];	

	$("#uri").hide();
	$("#tree").css("margin-top", $(obj).parent().prevAll().length * 29);
	
	$("#service .glyphicon-chevron-right").removeClass("glyphicon-chevron-right");
	$("#service .active").removeClass("active");
	$(obj).parent().addClass("active");
	$(obj).find(".glyphicon").addClass("glyphicon-chevron-right");
	
	showLoading();
	$.post(contextPath + 'grant/getResource', "serviceId=" + _id, function(r){
		hideLoading();
		if (r.result == -1) {
			initTree([{text:"资源", icon:"glyphicon glyphicon-home"}]);
		}
		else {
			initTree([treeUtils.decompressNode(r.tree)]);
		}
	});
	
	$.post(servicePath + "/monitorConf/getResourceUri", null, function(r){
		typeHeadSource = r.arrayList;
		for (var i in typeHeadSource) {
			typeHeadSourceMap[typeHeadSource[i]] = 1;
		}
	});
}

function addChild() {
	var oldValue = $("#addNodeType").val();
	$("#addNodeType").html("");	
	var selectNode = $('#tree').treeview('getSelected')[0];
	var nodeType = treeUtils.getNodeType(selectNode.icon)
		
	if (nodeType == -1 || nodeType == 0) {		
		if (oldValue == 1) {
			// 如果之前选择了菜单(nodeType=1)，则默认选择菜单
			$("#addNodeType").append('<option value="0">目录</option><option value="1" selected>菜单</option>');		
			noteTypeChange(1);
		}
		else {
			$("#addNodeType").append('<option value="0">目录</option><option value="1">菜单</option>');		
			noteTypeChange(0);
		}
	}
	else if (nodeType == 1) {
		// 菜单下面只能添加操作
		$("#addNodeType").append('<option value="2">操作</option>');
		noteTypeChange(2);
	}
	
	$('#addChild').modal('show');
}

function addChildOk() {
	var nodeName = $("#addNodeName").val();
	if (nodeName == "") {
		alertWarning("名称不能为空！");
		$("#addNodeName").focus();
		return;
	}
	
	var nodeType = $("#addNodeType").val();
	var icon = treeUtils.getNodeIcon(nodeType);
		
	
	var nodes = $('#tree').treeview('getNodes');
	var id = currentServiceId + "_" + nodes.length;
	
	var childNode = {text:nodeName,icon:icon,id:id};	
	var selectNode = $('#tree').treeview('getSelected')[0];
	if (!selectNode.nodes) selectNode.nodes = [];
	selectNode.nodes.push(childNode);	
	selectNode.state.expanded = true;
	
	initTree([$('#tree').treeview('getNode', 0)]);
	$("#addChild").modal("hide");
	saveResource();
}

function noteTypeChange(noteType) {
	var icon = treeUtils.getNodeIcon(noteType);
	$("#nodeGlyphicon").attr("class", icon);
}

function updateNode() {
	var selectNode = $('#tree').treeview('getSelected')[0];
	$("#updateNodeGlyphicon").attr("class", selectNode.icon);
	$("#updateNodeName").val(selectNode.text);
	$("#updateNode").modal("show");
}

function updateNodeOk() {
	var nodeName = $("#updateNodeName").val();
	if (nodeName == "") {
		alertWarning("名称不能为空！");
		return;
	}
	
	var selectedNode = $('#tree').treeview('getSelected')[0];
	selectedNode.text = nodeName;
	initTree([$('#tree').treeview('getNode', 0)]);
	$("#updateNode").modal("hide");
	saveResource();
}

function removeNode() {
	var callback = function () {
		var selectedNode = $('#tree').treeview('getSelected')[0];
		childrenId = [];
		var removeIds = treeUtils.getChildrenIds(selectedNode);
		
		var parentNode = $('#tree').treeview('getParent', selectedNode);
		if (parentNode.nodes.length == 1) {
			delete parentNode.nodes;
		}
		else {
			for (var i = 0; i < parentNode.nodes.length; i++) {
				if (parentNode.nodes[i].nodeId == selectedNode.nodeId) {
					parentNode.nodes.splice(i, 1);
					break;
				}
			}
		}
		
		initTree([$('#tree').treeview('getNode', 0)]);
		$("#uri").hide();	
		$("#removeConfig").modal("hide");
		saveResource(removeIds);
	}
	var selectNode = $('#tree').treeview('getSelected')[0];
	confirm("确定要删除'" + selectNode.text + "'及其子节点?", callback);
}


//>>>>>>>>>>>>>>>>>>>>>>>>>>move node>>>>>>>>>>>>>>>>>>>>>
var move = {};
move.ACTION_POSITION_TOP = 50
move.topNode = function() {
	var node = $('#tree').treeview('getSelected')[0];
	var nodeId = node.nodeId;	
	var parent = $('#tree').treeview('getParent', node);
	var tmpParentStr = JSON.stringify(parent);
	var tmpParent = JSON.parse(tmpParentStr);
	
	for (var i = 0; i < parent.nodes.length; i++) {
		if (i == 0) {
			parent.nodes[i] = node;
		}
		else if (nodeId >= parent.nodes[i].nodeId) {
			parent.nodes[i] = tmpParent.nodes[i-1];
		}
	}
	
	initTree([$('#tree').treeview('getNode', 0)]);
	var nodeId = $('#tree').treeview('getSelected')[0].nodeId;
	var top = $('#tree').find("[data-nodeid='" + nodeId + "']").position().top - this.ACTION_POSITION_TOP;
	$("#uri").css("margin-top", top);
	this.refreshMoveButton();
	saveResource();
}
move.upNode = function() {
	var node = $('#tree').treeview('getSelected')[0];
	var nodeId = node.nodeId;
	var tmpNodeStr = JSON.stringify(node);
	var tmpNode = JSON.parse(tmpNodeStr);
	var parent = $('#tree').treeview('getParent', node);
	
	var currentN = -1;
	for (var i = 0; i < parent.nodes.length; i++) {
		if (nodeId == parent.nodes[i].nodeId) {
			currentN = i;
			break;
		}
	}
	parent.nodes[currentN] = parent.nodes[currentN - 1];
	parent.nodes[currentN - 1] = tmpNode;
	
	initTree([$('#tree').treeview('getNode', 0)]);
	
	var nodeId = $('#tree').treeview('getSelected')[0].nodeId;
	var top = $('#tree').find("[data-nodeid='" + nodeId + "']").position().top - this.ACTION_POSITION_TOP;
	$("#uri").css("margin-top", top);
	
	this.refreshMoveButton();
	saveResource();
}
move.downNode = function() {
	var node = $('#tree').treeview('getSelected')[0];
	var nodeId = node.nodeId;
	var tmpNodeStr = JSON.stringify(node);
	var tmpNode = JSON.parse(tmpNodeStr);
	var parent = $('#tree').treeview('getParent', node);
	
	var currentN = -1;
	for (var i = 0; i < parent.nodes.length; i++) {
		if (nodeId == parent.nodes[i].nodeId) {
			currentN = i;
			break;
		}
	}
	parent.nodes[currentN] = parent.nodes[currentN + 1];
	parent.nodes[currentN + 1] = tmpNode;
	
	initTree([$('#tree').treeview('getNode', 0)]);
	
	var nodeId = $('#tree').treeview('getSelected')[0].nodeId;
	var top = $('#tree').find("[data-nodeid='" + nodeId + "']").position().top - this.ACTION_POSITION_TOP;
	$("#uri").css("margin-top", top);
	
	this.refreshMoveButton();
	saveResource();
}
move.refreshMoveButton = function() {
	$("#uri .glyphicon").hide();
	
	var node = $('#tree').treeview('getSelected')[0];
	var nodeId = node.nodeId;
	var parent = $('#tree').treeview('getParent', node);
	if (parent.nodes == undefined) {
		return;
	}	
	
	var currentN = -1;
	for (var i = 0; i < parent.nodes.length; i++) {
		if (nodeId == parent.nodes[i].nodeId) {
			currentN = i;
			break;
		}
	}
		
	if (parent.nodes.length == 1) {
		return;
	}
	
	if (currentN == 0) {
		$("#uri .glyphicon-arrow-down").show();
	}
	else if (currentN == parent.nodes.length - 1) {
		$("#uri .glyphicon-step-backward").show();
		$("#uri .glyphicon-arrow-up").show();	
	}
	else  {
		$("#uri .glyphicon-step-backward").show();
		$("#uri .glyphicon-arrow-up").show();
		$("#uri .glyphicon-arrow-down").show();
	}
}

//>>>>>>>>>>>>>>>>>>>>URI>>>>>>>>>>>>>>>>>
function addTypeHead(jObj) {
	jObj.typeahead({items:10,source:typeHeadSource});
}

function addUri() {
	var li = '<li class="list-group-item">\
		<input id="typeaheadId" type="text" class="form-control input-sm" data-provide="typeahead" style="width:370px;float:left;margin:-5px">\
		<a href="#this" class="glyphicon glyphicon-plus-sign" style="margin-left:12px;" onclick="addUriItem(this)"></a>\
		</li>';
	$("#uriUl").html(li);	
	
	addTypeHead($("#typeaheadId"));	
	$("#addUri").modal("show");
}

function addUriOk() {
	var isPass = true;
	var selectedNode = $('#tree').treeview('getSelected')[0];
	var uriArray = [];
	$("#uriUl [data-provide='typeahead']").each(function(){
		var uri = $.trim($(this).val());
		if (uri == "") {
			alertWarning("uri不能为空！");
			isPass = false;
			return;
		}
		var u = uri.split("?")[0];
		if (typeHeadSourceMap[u] != 1) {
			alertWarning("uri不存在！");
			isPass = false;
			return;
		}
		uriArray.push(uri);
	})
	if (!isPass) {
		return;
	}
	
	var parent = $('#tree').treeview('getParent', selectedNode);
	var parentType = treeUtils.getNodeType(parent.icon);
	// 父节点是菜单,表示是操作，则添加menuId作操作权限判断
	var menuId = (parentType == 1) ? ("&menuId=" + parent.id) : "";
	showLoading();
	$.post(contextPath + "grant/saveUri", "resId=" + selectedNode.id + "&uri=" + uriArray + menuId, function(r){
		hideLoading();
		alertSuccess("添加成功！");		
		$("#uriList li:gt(0)").remove();		
		var uriList = [];
		for (var i = 0; r.uri && i < r.uri.length; i++) {
			uriList.push({uri:r.uri[i], uriIndex:r.uriIndex[i]});
		}
		$("#uriList").append(template('uriListTemplate', uriList));
		$("#addUri").modal("hide");
	});
}

function addUriItem(obj) {
	var li = '<li class="list-group-item">\
		<input type="text" class="form-control input-sm" data-provide="typeahead" style="width:370px;float:left;margin:-5px">\
		<a href="#this" class="glyphicon glyphicon-minus-sign" style="margin-left:12px;" onclick="$(this).parent().remove()"></a>\
		</li>';
	$("#uriUl").append(li);
	addTypeHead($("#uriUl [data-provide='typeahead']").last());
}

function preRemoveUri(obj) {
	var callback = function () {
		var uri = $(obj).attr("data-uri");
		// 数组只有一条数据时，删除整条记录(uri=-1)
		uri = $(obj).parent().parent().find("li").length == 2 ? "-1" : uri;
		
		var resId = $('#tree').treeview('getSelected')[0].id;
		showLoading();
		var para = "resId=" + resId + "&uri=" + uri + "&uriIndex=" +  $(obj).attr("data-uri-index");
		$.post(contextPath + "grant/removeUri", para, function(r){
			hideLoading();
			$(obj).parent().remove();	
			alertSuccess("删除成功！");	
		});
	}
	confirm("确定删除'" + $(obj).prev().text() + "'?", callback);
}


