<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include.jsp" %>

<!-- ======================= header ===========================-->
<jsp:include page="/WEB-INF/views/headerMainMaintenance.jsp" />
<!-- =====================end header ==========================-->
	<SCRIPT type="text/javascript" src="resources/js/mainmaintenancegate.js?ver=${user.versionEspedsg}"></SCRIPT>
 	<style type = "text/css">
	.ui-datepicker { font-size:9pt;}
	</style>
	
 	<table width="100%" class="text11" cellspacing="0" border="0" cellpadding="0">
	<tr height="15"><td>&nbsp;</td></tr>
	<tr>
		<td>
		<%-- tab container component --%>
		<table width="100%" class="text11" cellspacing="0" border="0" cellpadding="0">
			<tr height="2"><td></td></tr>
				<tr height="25"> 
					<td width="20%" valign="bottom" class="tab" align="center" nowrap>
						<font class="tabLink">&nbsp;<spring:message code="systema.main.maintenance.label"/></font>&nbsp;
						<img style="vertical-align: middle;"  src="resources/images/list.gif" border="0" alt="general list">
					</td>
					<td width="80%" class="tabFantomSpace" align="center" nowrap><font class="tabDisabledLink">&nbsp;</font></td>	
				</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td>
		<%-- space separator --%>
	 		<table width="100%" class="tabThinBorderWhite" border="0" cellspacing="0" cellpadding="0">
	 	    <tr height="20"><td>&nbsp;</td></tr>
	 	    
			<%-- list component --%>
			<tr>
				<td width="2%">&nbsp;</td>
					
				<td width="100%">
				<table id="containerdatatableTable" width="90%" cellspacing="1" border="0" align="center">
			    	    <tr>
						<td class="text14">
						<table width="100%" id="mainList" class="display compact cell-border" >
							<thead>
							<tr>
								<th width="2%" class="tableHeaderField" align="center" >&nbsp;<spring:message code="systema.main.maintenance.main.gate.edit"/></th>
			                    <th width="80%" class="tableHeaderField" align="left" >&nbsp;<spring:message code="systema.main.maintenance.main.gate.description"/>&nbsp;</th>
			                    
			                </tr>  
			                </thead> 
			                <tbody >  
				            <c:forEach var="record" items="${model.list}" varStatus="counter">   
				               <tr class="tableRow" height="20" >
				              
				               <td width="2%" class="tableCell" style="border-style: solid;border-width: 0px 1px 1px 0px;border-color:#FAEBD7;" align="center">
				               	<c:choose>
				               		<c:when test="${(record.status == 'G' || record.status == 'Y') && not empty record.code}">
					               		<a id="alinkRecordId_${counter.count}" onClick="setBlockUI(this);" href="${record.code}<c:if test="${(record.status == 'G' || record.status == 'Y') && not empty record.pgm}">_${record.pgm}</c:if>.do?id=${record.dbTable}">
		               						<img src="resources/images/update.gif" border="0" alt="edit">
					               		</a>
				               		</c:when>
				               		<c:otherwise>
										<img src="resources/images/lock.gif" border="0" alt="edit">				               		
				               		</c:otherwise>
				               	</c:choose>	
				               </td>
				               <td class="tableCell" style="border-style: solid;border-width: 0px 1px 1px 0px;border-color:#FAEBD7;" width="80%" >
				               		<c:choose>
					               		<c:when test="${(record.status == 'G' || record.status == 'Y') && not empty record.code}">
					               			<a id="alinkRecordDesc_${counter.count}" onClick="setBlockUI(this);" href="${record.code}<c:if test="${(record.status == 'G' || record.status == 'Y') && not empty record.pgm}">_${record.pgm}</c:if>.do?id=${record.dbTable}">
		               							<font class="text16SkyBlue">&nbsp;&nbsp;${record.subject}&nbsp;</font>
		               						</a>
					               		</c:when>
					               		<c:otherwise>
					               			<font class="text16">&nbsp;&nbsp;${record.subject}&nbsp;</font>
					               		</c:otherwise>
				               		</c:choose>
				               </td>
				               
				            </tr> 
				            </c:forEach>
				            </tbody>
			            </table>
					</td>	
					</tr>
				</table>
				</td>
			</tr>
	 	    <tr height="20"><td>&nbsp;</td></tr>
	 		</table>
		</td>
	</tr>
</table>	
       
		
<!-- ======================= footer ===========================-->
<jsp:include page="/WEB-INF/views/footer.jsp" />
<!-- =====================end footer ==========================-->

