define(["require","jquery","underscore","backbone","bundle!ImportExportBundle","tenantImportExport/export/view/ExportDialogView","tenantImportExport/import/view/ImportDialogView","tenantImportExport/view/tenantTree","common/component/menu/ContextMenu","text!tenantImportExport/templates/tooltipTemplate.htm"],function(e){"use strict";var t=e("jquery"),n=e("underscore"),i=e("backbone"),o=e("bundle!ImportExportBundle"),r=e("tenantImportExport/export/view/ExportDialogView"),s=e("tenantImportExport/import/view/ImportDialogView"),a=e("tenantImportExport/view/tenantTree"),p=e("common/component/menu/ContextMenu"),l=e("text!tenantImportExport/templates/tooltipTemplate.htm"),c="organizations",h=i.View.extend({initialize:function(e){this.$container=t(e.container);var i=window.localStorage?JSON.parse(localStorage.getItem("selectedTenant")):null,h=e.selectedTenant||i,u=e.currentUser?e.currentUser.split("|")[1]||c:"/"===h.tenantUri?h.tenantId||c:c;this.splittedUri=n.compact(h.tenantUri.split("/")),this.splittedUri.splice(this.splittedUri.length-1,1),this.splittedUri.unshift(u);var d={tooltipContentTemplate:l,tenantId:h.id||u,comparator:e.comparator};e.removeContextMenuTreePlugin||(this.contextMenu=new p([{label:o["context.menu.option.export"],action:"export"},{label:o["context.menu.option.import"],action:"import"}],{hideOnMouseLeave:!0}),n.extend(d,{contextMenu:this.contextMenu}),this.listenTo(this.contextMenu,"option:export",this._onExport),this.listenTo(this.contextMenu,"option:import",this._onImport)),this.exportDialog=new r,this.importDialog=new s,this.tree=new a(d),this._initEvents()},render:function(){this.$container.append(this.tree.render().$el)},refreshTenant:function(e){var t=this.tree.getLevel(e);this.tree.getDataLayer(t).predefinedData=[],e?t.refresh():this.tree.refresh()},getTenant:function(){return this.selectedTenant},setTenant:function(e){this.selectedTenant=e},selectTenant:function(e){this.tree.select(e)},addTenant:function(e,t){var n=this.tree.getLevel(e);this.listenToOnce(n,"ready",function(){this.tree.addItem(e,this._processItem(t,!0))}),n.open()},updateTenant:function(e,t,n){this.tree.updateItem(this._processItem(t,!1,n),e)},removeTenant:function(e,t){var i,o=this.tree.getLevel(t);e=n.isArray(e)?e:[e],n.each(e,function(e){i=this.tree.getLevel(e),i&&i.remove()},this),o.refresh(),o.open()},remove:function(){this.tree.remove(),this.contextMenu&&this.contextMenu.remove()},_processItem:function(e,t,n){var i=e.tenantId||e.id,o={id:i,label:e.tenantName,uri:e.tenantFolderUri,tenantUri:e.tenantFolderUri,parentId:e.parentId};return t?{_node:!0,id:i,label:e.tenantName,value:o}:{id:e.tenantId,addToSelection:n,label:e.tenantName,value:o}},_initEvents:function(){this.expandedLevels=[],this.listenTo(this.tree,"selection:change",function(e){this.trigger("selection:change",n.compact(e)[0])}),this.listenTo(this.importDialog,"import:finished",function(e){this.refreshTenant(e),this.trigger("import:finished",e)}),this._recursivelyOpenLevels(this.tree.rootLevel)},_recursivelyOpenLevels:function(e){this.listenTo(e,"ready",function(){var e=this.tree.getLevel(this.splittedUri[0]);this.splittedUri.splice(0,1),e?(this.expandedLevels.push(e),this.tree.expand(e.id),this._recursivelyOpenLevels(e)):n.each(this.expandedLevels,function(e){this.stopListening(e,"ready")},this)})},_onExport:function(){this.exportDialog.openTenantDialog(this.selectedTenant)},_onImport:function(){this.importDialog.openDialog(this.selectedTenant)}});return h});