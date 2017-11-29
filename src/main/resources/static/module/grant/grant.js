
function queryPage(pageNumber) {	
	showLoading();
	$("#pageNumber").val(pageNumber);
	$.post(contextPath + 'grant/listRole', $("#queryForm").serialize(), function(data) {
		refreshPageData(data);
		hideLoading();
	});
}

var treeUtils = {};
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
treeUtils.childrenNode = [];
treeUtils.getChildrenNode = function(node, recursion) {
	if (!recursion) {
		this.childrenNode = [];
	}	
	else {
		this.childrenNode.push(node);
	}
	
	if (node.nodes) {
		for (var i = 0; i < node.nodes.length; i++) {
			this.getChildrenNode(node.nodes[i], true);
		}
	}
	return this.childrenNode;
}
treeUtils.getCheckedChildrenNode = function(node) {
	var r = [];
	var checkedChildrenNode = this.getChildrenNode(node);
	for (var i = 0; i < checkedChildrenNode.length; i++) {
		if (checkedChildrenNode[i].state.checked == true) {
			r.push(checkedChildrenNode[i]);
		}
	}
	return r;
}
treeUtils.loadIndeterminate = function(newNode, node, resMap) {
	if (!newNode.state.checked) {
		return;
	}
	
	if (!node.n) {
		newNode.backColor = "#428bca";
		newNode.color = "white";	
	}
	else if (node.n) {
		if (this.hasNoChecked(node.n, resMap)) {
			newNode.backColor = "green";
			newNode.color = "white";	
		}
		else {
			newNode.backColor = "#428bca";
			newNode.color = "white";
		}
	}
}
treeUtils.clickIndeterminate = function(node) {
	if (node.nodeId == 0) return;
	
	var parent = $('#tree').treeview('getParent', node);
	if (node.state.checked) parent.state.checked = true;
		
	var nodeLen = treeUtils.getChildrenNode(parent).length;
	var checkLen = treeUtils.getCheckedChildrenNode(parent).length;	
	if (nodeLen != checkLen) {		
		parent.backColor = "green";
		parent.color = "white";		
	}
	else {
		parent.backColor = "#428bca";
		parent.color = "white";				
	}	
	
	if (parent.state.checked == false) {
		parent.backColor = "white";
		parent.color = "black";
	}
	this.clickIndeterminate(parent);
}
treeUtils.hasNoChecked = function(nodes, resMap) {
	for (var i = 0; i < nodes.length; i++) {
		if (resMap[nodes[i].id] != 1) return true;
		if (nodes[i].n) {
			if (this.hasNoChecked(nodes[i].n, resMap)) return true;
		}
	}	
	return false;
}
treeUtils.decompressNode = function(node, resMap) {
	var newNode = {};
	newNode.text = node.t;
	newNode.icon = treeUtils.getNodeIcon(node.i);
	newNode.id = node.id;
	newNode.state = {};
	
	if (!node.id || resMap[node.id] == 1) {
		// 存在已经选择的节点
		for (i in resMap) {
			newNode.state.checked = true;
			break;
		}
	}
	
	// 装载时半选状态
	this.loadIndeterminate(newNode, node, resMap);	
	
	if (node.n) {
		newNode.nodes = [];
		for (var i = 0; i < node.n.length; i++) {
			newNode.nodes.push(this.decompressNode(node.n[i], resMap));
		}
	}
	return newNode;
}


var currentServiceId;
function onService(obj, _id) {
	currentServiceId = _id;
	$("#tree").show();
	
	var top = $(obj).parent().prevAll().length * 29;	
	$("#tree").css("margin-top", top);
	
	$("#service .glyphicon-chevron-right").removeClass("glyphicon-chevron-right");
	$("#service .active").removeClass("active");
	$(obj).parent().addClass("active");
	$(obj).find(".glyphicon").addClass("glyphicon-chevron-right");
	
	$('#loading').modal('show');
	var roleId = $("#grantRoleId").val();
	$.post(contextPath + 'grant/getAuthorize', "serviceId=" + _id + "&roleId=" + roleId, function(r){		
		if (r.result == -1) {
			var tree = [{text:"资源", icon:"glyphicon glyphicon-home"}];
			initTree(tree);
			$("#viewFolderN").text(0);
			$("#viewMenuN").text(0);
			$("#viewOpN").text(0);
		}
		else {
			var resMap = [];
			for (var i = 0;r.resIds && i < r.resIds.length; i++) {
				if (r.resIds[i]) {
					resMap[r.resIds[i]] = 1;
				}				
			} 
			initTree([treeUtils.decompressNode(r.tree, resMap)]);			
			refreshHint();			
		}
		$('#loading').modal('hide');
	});
}

function refreshHint() {
	var folderN = 0;
	var menuN = 0;
	var opN = 0;
	
	var node = $('#tree').treeview('getNode', 0);
	var checkedNode = treeUtils.getCheckedChildrenNode(node);
	for (var i = 0; i < checkedNode.length; i++) {
		var noteType = treeUtils.getNodeType(checkedNode[i].icon);
		if (noteType == 0) {
			folderN++;
		}
		else if (noteType == 1) {
			menuN++;
		}
		else if (noteType == 2) {
			opN++;
		}
	}
	
	$("#viewFolderN").text(folderN);
	$("#viewMenuN").text(menuN);
	$("#viewOpN").text(opN);
}

function initTree(tree) {
	$('#tree').treeview({
		data:tree,
		levels:2,
		showCheckbox:true,
		highlightSelected:false,
		onNodeChecked:function(event, node) {
			var n = $('#tree').treeview('getNode', node.nodeId);
			n.backColor = "#428bca";
			n.color = "white";
			
			var childrenNode = treeUtils.getChildrenNode(node);
			for (var i = 0; i < childrenNode.length; i++) {				
				var node = $('#tree').treeview('getNode', childrenNode[i].nodeId);				
				node.state.checked = true;
				node.backColor = "#428bca";
				node.color = "white";
			}
			treeUtils.clickIndeterminate(node);			
			refreshHint();			
		},
		onNodeUnchecked:function(event, node) {
			var n = $('#tree').treeview('getNode', node.nodeId);
			n.backColor = "white";
			n.color = "black";
			
			var childrenNode = treeUtils.getChildrenNode(node);	
			for (var i = 0; i < childrenNode.length; i++) {				
				var node = $('#tree').treeview('getNode', childrenNode[i].nodeId);				
				node.state.checked = false;
				node.backColor = "white";
				node.color = "black";				
			}			
			treeUtils.clickIndeterminate(node);		
			refreshHint();
		}
	})
}

function grant(roleId, roleName) {
	// 初始化页面
	$("#grantRoleId").val(roleId);
	$("#grantRoleName").text(roleName);
	currentServiceId = undefined;
	$("#tree").hide();	
	$("#service .glyphicon-chevron-right").removeClass("glyphicon-chevron-right");
	$("#service .active").removeClass("active");
	
	$("#viewFolderN").text(0);
	$("#viewMenuN").text(0);
	$("#viewOpN").text(0);
	
	$('#grantModal').modal('show');
}

function authorize() {
	if (!currentServiceId) {
		alertWarning("请选择微服务!");
		return;
	}
	
	var node = $('#tree').treeview('getNode', 0);
	var checkedNode = treeUtils.getCheckedChildrenNode(node);
	var checkedIds = [];
	for (var i = 0; i < checkedNode.length; i++) {
		checkedIds.push(checkedNode[i].id);
	}
	
	showLoading();
	var para = "roleId=" + $("#grantRoleId").val() + "&serviceId=" + currentServiceId + "&resIds=" + checkedIds;
	$.post(contextPath + 'grant/saveAuthorize', para, function(r) {
		$('#grantModal').modal('hide');
		hideLoading();
		alertSuccess("保存成功！");
	});
}

