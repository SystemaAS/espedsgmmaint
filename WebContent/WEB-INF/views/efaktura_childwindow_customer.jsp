<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include.jsp" %>

<!-- ======================= header =====================================-->
<jsp:include page="/WEB-INF/views/headerEfakturaChildWindows.jsp" />
<!-- =====================end header ====================================-->

	<%-- specific jQuery functions for this JSP (must reside under the resource map since this has been
	specified in servlet.xml as static <mvc:resources mapping="/resources/**" location="WEB-INF/resources/" order="1"/> --%>
	<SCRIPT type="text/javascript" src="resources/js/efaktura_childwindow.js?ver=${user.versionEspedsg}"></SCRIPT>
	
	<table width="90%" height="500px" class="tableBorderWithRoundCorners3D_RoundOnlyOnBottom" cellspacing="0" border="0" cellpadding="0">
		<tr height="5"><td colspan="2"></td></tr>
		<tr>
			<td colspan="3" class="text14Bold">&nbsp;Kunde søk</td>
		</tr>
		<tr height="20"><td colspan="2"></td></tr>
		<tr>
		<td valign="top">
		<form action="efaktura_childwindow_customer.do?action=doFind" name="searchCustomerForm" id="searchCustomerForm" method="post">
			<input type="hidden" name="ctype" id="ctype" value="${model.container.ctype}">
			<%-- =====================================================  --%>
          	<%-- Here we have the search [Customer] popup window --%>
          	<%-- =====================================================  --%>
          		<%-- this container table is necessary in order to separate the datatables element and the frame above, otherwise
			 	the cosmetic frame will not follow the whole datatable grid including the search field... --%>
				<table id="containerdatatableTable" cellspacing="2" align="left" width="100%">
					<tr height="5"><td></td></tr>
					<tr>
					<td>
						<table>
						<tr>
							<td class="text14">&nbsp;Kundenr.</td>
							<td class="text14">&nbsp;<input type="text" class="inputText" name="sokknr" id="sokknr" size="8" maxlength="8" value="${model.container.sokknr}"></td>
						
							<td class="text14">&nbsp;&nbsp;&nbsp;Navn</td>
							<td class="text14">&nbsp;<input type="text" class="inputText" name="soknvn" id="soknvn" size="15" maxlength="35" value="${model.container.soknvn}"></td>
						
							<%--
							<td class="text14">&nbsp;&nbsp;&nbsp;Postnr/Postadresse/Land</td>
							<td class="text14">&nbsp;<input type="text" class="inputText" name="kunpnsted" id="kunpnsted" size="15" maxlength="10" value="${model.container.kunpnsted}"></td>
							 --%>
							 
							<td class="text14">&nbsp;</td>
	           				<td align="right">&nbsp;<input class="inputFormSubmit" type="submit" name="submit" value='Søk'>
		           		</tr>
		           		
		           		</table>
					</td>
					</tr>
					<%-- Validation errors --%>
					<spring:hasBindErrors name="record"> <%-- name must equal the command object name in the Controller --%>
					<tr>
						<td colspan="20">
			            	<table align="left" border="0" cellspacing="0" cellpadding="0">
			            	<tr>
							<td class="textError">					
					            <ul>
					            <c:forEach var="error" items="${errors.allErrors}">
					                <li >
					                	<spring:message code="${error.code}" text="${error.defaultMessage}"/>
					                </li>
					            </c:forEach>
					            </ul>
							</td>
							</tr>
							</table>
						</td>
					</tr>
					</spring:hasBindErrors>
										
					
					<tr><td><hr size="1" width="100%"/></td></tr>								           		
	           		<tr height="15"><td></td></tr>
					
					<tr class="text12" >
					<td class="ownScrollableSubWindowDynamicWidthHeight" width="100%" style="height:50em;">
					<%-- this is the datatables grid (content)--%>
					<table id="customerList" class="display compact cell-border" width="100%">
						<thead>
						<tr class="tableHeaderField">
						    <th class="text14">&nbsp;Kundenr</th>   
		                    <th class="text14">&nbsp;Navn</th>
		                    <th class="text14">&nbsp;Adresse</th>
		                    <th class="text14">&nbsp;Postnr./Postadresse/Land</th>
		                    
		                </tr> 
		                </thead>
		                
		                <tbody>
		                <c:forEach var="record" items="${model.customerList}" varStatus="counter">    
			               <c:choose>           
			                   <c:when test="${counter.count%2==0}">
			                       <tr class="text14" >
			                   </c:when>
			                   <c:otherwise>   
			                       <tr class="text14" >
			                   </c:otherwise>
			               </c:choose>
			               <td style="cursor:pointer;" class="text14MediumBlue" id="kundnr_${record.kundnr}@navn_${record.navn}@counter_${counter.count}">&nbsp;${record.kundnr}</td>
			               <td class="text14">&nbsp;${record.navn}</td>
			               <td class="text14">&nbsp;${record.adr1}</td>
			               <td class="text14">&nbsp;${record.adresse}</td>
			               
			            </tr> 
			            </c:forEach>
			            </tbody>
		            </table>
		            </td>
	           		</tr>
        			</table>
				
		</form>	
		</td>
		</tr>
	</table> 
