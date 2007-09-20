<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/PagenationTag.tld" prefix="custom" %>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="edu.wustl.catissuecore.actionForm.AdvanceSearchForm"%>
<%@ page import="edu.wustl.catissuecore.util.global.Constants"%>
<%@ page import="edu.wustl.catissuecore.util.global.Utility"%>
<%@ page import="edu.wustl.catissuecore.util.global.Variables"%>

<script src="jss/script.js"></script>
<script type="text/javascript" src="jss/ajax.js"></script> 
<style>
.active-column-0 {width:30px}
tr#hiddenCombo
{
 display:none;
}
</style>
<head>
<%
	
	int pageNum = Integer.parseInt((String)request.getAttribute(Constants.PAGE_NUMBER));
	
	int totalResults = (Integer)session.getAttribute(Constants.TOTAL_RESULTS);
	int numResultsPerPage = Integer.parseInt((String)session.getAttribute(Constants.RESULTS_PER_PAGE));
	String pageName = "SpreadsheetView.do";	
	String checkAllPages = (String)session.getAttribute("checkAllPages");
	AdvanceSearchForm form = (AdvanceSearchForm)session.getAttribute("advanceSearchForm");
	List columnList = (List) session.getAttribute(Constants.SPREADSHEET_COLUMN_LIST);
	if(columnList==null)
		columnList = (List) request.getAttribute(Constants.SPREADSHEET_COLUMN_LIST);

	columnList.add(0," ");
	List dataList = (List) request.getAttribute(Constants.PAGINATION_DATA_LIST);
	
	String pageOf = (String)request.getAttribute(Constants.PAGEOF);
	String title = pageOf + ".searchResultTitle";
	boolean isSpecimenData = false;	
	int IDCount = 0;
		
	%>
		

	<script language="javascript">
		var colZeroDir='ascending';


		function onAddToCart()
		{
			var isChecked = updateHiddenFields();
			var chkBox = document.getElementById('checkAll');
			var isCheckAllAcrossAllChecked = chkBox.checked;
			
		    if(isChecked == "true")
		    {
			    var pageNum = "<%=pageNum%>";
				var action;
                var isQueryModule = "<%=pageOf.equals(Constants.PAGEOF_QUERY_MODULE)%>";
                <%if (pageOf.equals(Constants.PAGEOF_QUERY_MODULE))
                {
                %>
				
				 action = "QueryAddToCart.do?operation=add&pageNum="+pageNum+"&isCheckAllAcrossAllChecked="+isCheckAllAcrossAllChecked;
				  document.forms[0].target = "gridFrame";
				<%} else {%>
				
				
			     action = "ShoppingCart.do?operation=add&pageNum="+pageNum+"&isCheckAllAcrossAllChecked="+isCheckAllAcrossAllChecked ;
				 document.forms[0].target = "myframe1";
				<%}%>

				document.forms[0].operation.value="add";
				document.forms[0].action = action;
				document.forms[0].submit();
			}
			else
			{
				alert("Please select at least one checkbox");
			}
		}
		
		function onExport()
		{
			var isChecked = updateHiddenFields();
			  var pageNum = "<%=pageNum%>";
			var chkBox = document.getElementById('checkAll');
			var isCheckAllAcrossAllChecked = chkBox.checked;
		    if(isChecked == "true")
		    {
				var action = "SpreadsheetExport.do?pageNum="+pageNum+"&isCheckAllAcrossAllChecked="+isCheckAllAcrossAllChecked ;
				document.forms[0].operation.value="export";
				document.forms[0].action = action;
				//document.forms[0].target = "_blank";
				document.forms[0].submit();
			}
			else
			{
				alert("Please select at least one checkbox");
			}
		}
		//function that is called on click of Define View button for the configuration of search results
		function onSimpleConfigure()
		{
				action="ConfigureSimpleQuery.do?pageOf=pageOfSimpleQueryInterface";
				document.forms[0].action = action;
				document.forms[0].target = "_parent";
				document.forms[0].submit();
		}

		function onAdvanceConfigure()
		{
				action="ConfigureAdvanceSearchView.do?pageOf=pageOfQueryResults";
				document.forms[0].action = action;
				document.forms[0].target = "myframe1";
				document.forms[0].submit();
		}
		function onQueryResultsConfigure()
		{
			action="DefineQueryResultsView.do?pageOf=pageOfQueryModule";
			document.forms[0].action = action;
			document.forms[0].target = "<%=Constants.GRID_DATA_VIEW_FRAME%>";
			document.forms[0].submit();
		}
		function onRedefineSimpleQuery()
		{
			action="SimpleQueryInterface.do?pageOf=pageOfSimpleQueryInterface&operation=redefine";
			document.forms[0].action = action;
			document.forms[0].target = "_parent";
			document.forms[0].submit();
		}
		function onRedefineAdvanceQuery()
		{
			action="AdvanceQueryInterface.do?pageOf=pageOfAdvanceQueryInterface&operation=redefine";
			document.forms[0].action = action;
			document.forms[0].target = "_parent";
			document.forms[0].submit();
		}
		function onRedefineDAGQuery()
		{
			waitCursor();
			document.forms[0].action='SearchCategory.do?currentPage=resultsView';
			document.forms[0].target = "_parent";
			document.forms[0].submit();
			hideCursor();
		}
		var selected;

		function addCheckBoxValuesToArray(checkBoxName)
		{
			var theForm = document.forms[0];
		    selected=new Array();
		
		    for(var i=0,j=0;i<theForm.elements.length;i++)
		    {
		 	  	if(theForm[i].type == 'checkbox' && theForm[i].checked==true)
			        selected[j++]=theForm[i].value;
			}
		}
		
		function setDefaultView(element)
		{
			action="DefaultSpecimenView.do?pageOf=pageOfQueryResults&<%=Constants.SPECIMENT_VIEW_ATTRIBUTE%>="+element.checked+"&view=<%=Constants.SPECIMEN%>"+"&isPaging=false";
			document.forms[0].action = action;
			document.forms[0].target = "myframe1";
			document.forms[0].submit();
		}
		function callAction(action)
		{
			document.forms[0].action = action;
			document.forms[0].submit();
		}
		function setCheckBoxState()
		{
			var chkBox = document.getElementById('checkAll');
			var isCheckAllAcrossAllChecked = chkBox.checked;
		<%	if(checkAllPages != null && checkAllPages.equals("true"))
			{ %>
			chkBox.checked = true;
				rowCount = mygrid.getRowsNum();
				for(i=1;i<=rowCount;i++)
				{
					var cl = mygrid.cells(i,0);
					if(cl.isCheckbox())
					cl.setChecked(true);
				}
		<%	} %>
		}
//this function is called after executing ajax call from checkAllOnThisPage function.
function checkAllOnThisPageResponse()
{
}

//document.forms[0].checkAllPages.value = true;

	</script>
	<%
		String configAction = new String();
		String redefineQueryAction = new String();
		if(pageOf.equals(Constants.PAGEOF_SIMPLE_QUERY_INTERFACE))
		{
			configAction = "onSimpleConfigure()";
			redefineQueryAction = "onRedefineSimpleQuery()";
		}
		else if(pageOf.equals("pageOfQueryModule"))
		{
			configAction = "onQueryResultsConfigure()";
			redefineQueryAction = "onRedefineDAGQuery()";
		}
		else
		{
			configAction = "onAdvanceConfigure()";
			redefineQueryAction = "onRedefineAdvanceQuery()";
		}
	%>
	<!-- Mandar : 434 : for tooltip -->
	<script language="JavaScript" type="text/javascript" src="jss/javaScript.js"></script>
</head>
<body onload="setCheckBoxState()">
<table summary="" cellpadding="0" cellspacing="0" border="0" width="100%" height="100%">
<tr>
	<td >
		<html:errors /> <!--Prafull:Added errors tag inside the table-->
	</td>
</tr>
<html:form action="<%=Constants.SPREADSHEET_EXPORT_ACTION%>">
<html:hidden property="checkAllPages" value=""/>	

	<%
		if(dataList == null && pageOf.equals(Constants.PAGEOF_QUERY_RESULTS))
		{
		%>
			<bean:message key="advanceQuery.noRecordsFound"/>
		<%}
		else if(dataList != null && dataList.size() != 0)
		{
	%>
		<!-- 
			Patch ID: Bug#3090_28
			Description: The width of <td> are adjusted to fit into the iframe. 
			These changes were made to remove the extra white space on the data view/spreadsheet view page. 
		-->
		<tr height="3%">
			 <td  class="formTitle" width="100%">
				<bean:message key="<%=title%>"/>
			 </td>
		</tr>	
		
		<tr height="5%">
			<td class="dataPagingSection">					
				<custom:test name="Search Results" pageNum="<%=pageNum%>" totalResults="<%=totalResults%>" numResultsPerPage="<%=numResultsPerPage%>" pageName="<%=pageName%>"  showPageSizeCombo="<%=true%>" recordPerPageList="<%=Constants.RESULT_PERPAGE_OPTIONS%>" />
				<html:hidden property="<%=Constants.PAGEOF%>" value="<%=pageOf%>"/>
				<html:hidden property="isPaging" value="true"/>			
			</td>
		</tr>
		<%
		if(pageOf.equals(Constants.PAGEOF_QUERY_RESULTS))
		{			
			String []selectedColumns=form.getSelectedColumnNames();
		%>
		
		<tr id="hiddenCombo" rowspan="4" height="2%">
			<td class="formField" colspan="4">
	<!-- Mandar : 434 : for tooltip -->
	   			<html:select property="selectedColumnNames" styleClass="selectedColumnNames"  size="1" styleId="selectedColumnNames" multiple="true"
				 onmouseover="showTip(this.id)" onmouseout="hideTip(this.id)">
	   				<%
					for(int j=0;j<selectedColumns.length;j++)
	   				{
	   				%>
						<html:option value="<%=selectedColumns[j]%>"><%=selectedColumns[j]%></html:option>
					<%
	   				}
	   				%>
	   	 		</html:select>
			</td>
		</tr>
		<% } 
		%>
		
		<tr height="80%" valign="top" width="100%">
			<td  width="100%" valign="top">
<!--  **************  Code for New Grid  *********************** -->
				<script>
					/* 
						to be used when you want to specify another javascript function for row selection.
						useDefaultRowClickHandler =1 | any value other than 1 indicates you want to use another row click handler.
						useFunction = "";  Function to be used. 	
					*/
					var useDefaultRowClickHandler =1;
					var useFunction = "search";	
				</script>
				<%@ include file="/pages/content/search/AdvanceGrid.jsp" %>
<!--  **************  Code for New Grid  *********************** -->
			</td>
		</tr>

		<tr height="5%" width="100%" valign="top">
		<td>
			<table summary="" cellpadding="0" cellspacing="0" border="0" width="100%" height="100%" valign="top">
			<tr>
					<td width="5%" nowrap valign="top">
						<input type='checkbox' name='checkAll2' id='checkAll2' onClick='checkAllOnThisPage(this)'>
						<span class="formLabelNoBackGround"><bean:message key="buttons.checkAllOnThisPage" /></span>
						<input type='checkbox' name='checkAll' id='checkAll' onClick='checkAllAcrossAllPages(this)'>
						<span class="formLabelNoBackGround"><bean:message key="buttons.checkAll" /></span>
					</td>
					<%
						Object obj = session.getAttribute(Constants.SPECIMENT_VIEW_ATTRIBUTE);
						boolean isDefaultView = (obj!=null);
					%>
					<td nowrap width="5%" valign="top">
					<%if(pageOf.equals(Constants.PAGEOF_QUERY_RESULTS)){%>
						<input type='checkbox' <%if (isDefaultView){%>checked='checked' <%}%>name='checkDefaultSpecimenView' id='checkDefaultSpecimenView' onClick='setDefaultView(this)'>
						<span class="formLabelNoBackGroundWithSize6"><bean:message key="buttons.defaultSpecimenView" /></span>&nbsp;
					<%}else{%>
						&nbsp;
					<%}%>
					</td>
					<td width="70%" align="right" valign="top">
						&nbsp;
					</td>
					<td width="5%" nowrap align="right" valign="top">
					<%if(pageOf.equals(Constants.PAGEOF_QUERY_RESULTS) || pageOf.equals(Constants.PAGEOF_QUERY_MODULE) ){
						
					%>
						<html:button styleClass="actionButton" property="addToCart" onclick="onAddToCart()">
							<bean:message key="buttons.addToCart"/>
						</html:button>&nbsp;
				                        
					<%}else
				       {%>
						&nbsp;
					<%}%>
					</td>
					<td width="5%" nowrap align="right" valign="top">
						<html:button styleClass="actionButton" property="exportCart" onclick="onExport()">
							<bean:message key="buttons.export"/>
						</html:button>&nbsp;
					</td>
					<td width="5%" nowrap align="right" valign="top">
						<html:button styleClass="actionButton" property="configureButton" onclick="<%=configAction%>">
							<bean:message  key="buttons.configure" />
						</html:button>&nbsp;
					</td>
					<td width="5%" nowrap align="right" valign="top">
						<html:button styleClass="actionButton" property="redefineButton" onclick="<%=redefineQueryAction%>">
							<bean:message  key="buttons.redefineQuery" />
						</html:button>&nbsp;
					</td>
			</tr>
			</table>
			</td>
		</tr>
	<% } %>

	<tr>
		<td><html:hidden property="operation" value=""/></td>
	</tr>
	<input type="hidden" name="isQuery" value="true">
</html:form>
</table>