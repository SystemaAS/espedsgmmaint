<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include.jsp" %>

<!-- ======================= header ===========================-->
<jsp:include page="/WEB-INF/views/headerEfaktura.jsp" />
<!-- =====================end header ==========================-->
	<%-- specific jQuery functions for this JSP (must reside under the resource map since this has been
		specified in servlet.xml as static <mvc:resources mapping="/resources/**" location="WEB-INF/resources/" order="1"/> --%>
	<SCRIPT type="text/javascript" src="resources/js/efakturaglobal_edit.js?ver=${user.versionEspedsg}"></SCRIPT>	
	<SCRIPT type="text/javascript" src="resources/js/efaktura_mainlist.js?ver=${user.versionEspedsg}"></SCRIPT>
	
	<style type = "text/css">
	.ui-datepicker { font-size:9pt;}
	</style>

<table width="100%"  class="text14" cellspacing="0" border="0" cellpadding="0">
	<tr>
	<td>
	<%-- tab container component --%>
	<table width="100%"  class="text14" cellspacing="0" border="0" cellpadding="0">
		<tr height="2"><td></td></tr>
		<tr height="25"> 
			<td width="20%" valign="bottom" class="tab" align="center" nowrap>
				<a style="display:block;" href="efaktura_mainlist.do?action=doFind">
					<img style="vertical-align:middle;" src="resources/images/bulletGreen.png" width="6px" height="6px" border="0" alt="efaktura log">
					<font class="tabLink"><spring:message code="systema.efaktura.mainlist.tab"/></font>
				</a>
			</td>
			<%--
			<td width="1px" class="tabFantomSpace" align="center" nowrap><font class="tabDisabledLink">&nbsp;</font></td>
			<td width="20%" valign="bottom" class="tabDisabled" align="center" nowrap>
				<a style="display:block;" href="transportdisp_workflow_getTrip.do?user=${user.user}&tuavd=${searchFilter.avd}&tupro=${searchFilter.tur}">
					<img style="vertical-align:bottom;" src="resources/images/list.gif" border="0" alt="general list">
					<font class="tabDisabledLink"><spring:message code="systema.transportdisp.workflow.trip.tab"/></font>
				</a>
			</td>
			 --%>
			<td width="80%" class="tabFantomSpace" align="center" nowrap><font class="tabDisabledLink">&nbsp;</font></td>
		</tr>
	</table>
	</td>
	</tr>
	<input type="hidden" name="applicationUser" id="applicationUser" value='${user.user}'>
	<input type="hidden" name="language" id="language" value='${user.usrLang}'>
	<%-- Validation errors --%>
	<spring:hasBindErrors name="record"> <%-- name must equal the command object name in the Controller --%>
	<tr>
		<td>
           	<table width="100%" align="left" border="0" cellspacing="0" cellpadding="0">
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
	
		<tr>
		<td>
			<%-- this table wrapper is necessary to apply the css class with the thin border --%>
			<table id="wrapperTable" class="tabThinBorderWhite" width="100%" cellspacing="1" border="0">
			<tr height="5"><td></td></tr> 
						
			<%-- Datatables component --%>
			<%-- Log records --%>
			<tr>
				<td>
				<form name="searchForm" id="searchForm" action="efaktura_mainlist.do?action=doFind" method="post" >
				<input type="hidden" name="todo" id="todo" value=''>
				<table id="containerdatatableTable" width="80%" cellspacing="2" align="left">
				<tr>
					<td class="text14" title="avd">&nbsp;<spring:message code="systema.efaktura.mainlist.label.avd"/>
						<%--
						<a href="javascript:void(0);" onClick="window.open('todo_childwindow_avd.do?action=doInit','avdWin','top=150px,left=300px,height=600px,width=800px,scrollbars=no,status=no,location=no')">
 							<img id="imgAvdSearch" align="bottom" style="cursor:pointer;" src="resources/images/find.png" height="13px" width="13px" border="0" alt="search">
 						</a>
 						 --%>
					</td>
					<td class="text14" title="faktnr">&nbsp;<spring:message code="systema.efaktura.mainlist.label.xffn.faktnr"/></td>
					<td class="text14" title="kundenr">&nbsp;<spring:message code="systema.efaktura.mainlist.label.xfkn.kundenr"/>
						<a href="javascript:void(0);" onClick="window.open('efaktura_childwindow_customer.do?action=doInit&ctype=s','customerWin','top=300px,left=50px,height=800px,width=900px,scrollbars=no,status=no,location=no')">
							<img id="imgCustomerSearch" align="bottom" style="cursor:pointer;" src="resources/images/find.png" height="13px" width="13px" border="0" alt="search">
						</a>
					</td>
					<td class="text14" title="rfa">&nbsp;<spring:message code="systema.efaktura.mainlist.label.herfa.agentref"/></td>
					<td class="text14" title="from">&nbsp;<spring:message code="systema.efaktura.mainlist.label.hesdf.from"/></td>
					<td class="text14" title="to">&nbsp;<spring:message code="systema.efaktura.mainlist.label.hesdt.to"/></td>
					<td class="text14" title="status">&nbsp;<spring:message code="systema.efaktura.mainlist.label.xfst.status"/></td>
					
				</tr>
				<tr>	
					<td class="text14">
		        		<input type="text" class="inputText" name="avd" id="avd" size="5" maxlength="6" value='${searchFilter.avd}'>
		        	</td>
		        	<td class="text14">
			        	<input type="text" class="inputText" name="faktnr" id="faktnr" size="10" maxlength="15" value='${searchFilter.faktnr}'>
		        	</td>
		        	<td class="text14">
			        	<input type="text" class="inputText" name="kundenr" id="kundenr" size="10" maxlength="15" value='${searchFilter.kundenr}'>
			        	
		        	</td>
		        	<td class="text14">
			        	<input type="text" class="inputText" name="rfa" id="rfa" size="15" maxlength="15" value='${searchFilter.rfa}'>
		        	</td>
		        	<td class="text14">
			        	<input type="text" class="inputText" name="from" id="from" size="9" maxlength="8" value='${searchFilter.from}'>
		        	</td>
		        	<td class="text14">
			        	<input type="text" class="inputText" name="to" id="to" size="9" maxlength="8" value='${searchFilter.to}'>
			        </td>
			        <td class="text14">
			        	<select class="selectMediumBlueE2" name="status" id="status">
           					<option value="*" <c:if test="${searchFilter.status == '*'}"> selected </c:if> >Alle</option>
          					<option value="O" <c:if test="${searchFilter.status == 'O'}"> selected </c:if> >Received</option>
          					<option value="C" <c:if test="${searchFilter.status == 'C'}"> selected </c:if> >Sendt</option>
          					<option value=" "  <c:if test="${searchFilter.status == ' '}"> selected </c:if> >Ikke sendt</option>
          					<option value="E" <c:if test="${searchFilter.status == 'E'}"> selected </c:if> >Error</option>
          					<option value="A" <c:if test="${searchFilter.status == 'A'}"> selected </c:if> >Annet</option>
          					
          				</select>
			        </td>
			        <td class="text14">
			        	<input class="inputFormSubmit" type="submit" name="submit" value='Søk'>
			        </td>			
				</tr>
				</form>
				<tr height="2"><td></td></tr>
				</table>
				</td>
			</tr>
			<tr>
				<td>
				<table id="containerdatatableTable" width="100%" cellspacing="2" align="left">	
				<tr>
					<td class="text11">
					<table id="logRecords" class="display compact cell-border" width="100%" cellspacing="0">
						<thead>
						<tr class="tableHeaderField">
							<th class="text14"><spring:message code="systema.efaktura.mainlist.label.avd"/>/<spring:message code="systema.efaktura.mainlist.label.opd"/></th>   
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.xffn.faktnr"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.xfkn.kundenr"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.herfa.agentref"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hedtop.date"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hesdf.from"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hesdt.to"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.henas.avs"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.henak.mott"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hekdpl.prkd"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hent.ant"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hevkt.vkt"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.hem3.m3"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.helm.lm"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.xpdf.pdf"/></th>
		                    <th class="text14"><spring:message code="systema.efaktura.mainlist.label.xkifs.xml"/></th>
		                    <th class="text14">
		                    	<img onMouseOver="showPop('status_info');" onMouseOut="hidePop('status_info');" width="12px" height="12px" src="resources/images/info3.png" border="0" alt="info">
		                    	Status
		                    	<div class="text14" style="position: relative;" align="left">
									<span style="position: absolute; top:0px; left:-150px; width:200px;" id="status_info" class="popupWithInputText"  >
										<font class="text14" >
					           		 	<p>Status</p>
					           		 	<ul>
					           		 		<li><b>blank</b>&nbsp;Ikke sendt</li>
					           		 		<li><b>A</b>&nbsp;Annet.</li>
					           				<li><b>C</b>&nbsp;Sendt: sendt til mottakspunkt.</li>
					           				<li><b>E</b>&nbsp;Error: Fejl ved sending.</li>
					           				<li><b>N</b>&nbsp;Warning: Faktura mangler fakturalinjer.</li>
					           				<li><b>O</b>&nbsp;Received er OK.</li>
					           				<li><b>S</b>&nbsp;Info: Faktura er kredittert.</li>
					           				<li><b>T</b>&nbsp;Warning: PDF av faktura er ikke klar.</li>
					           				<li><b>V</b>&nbsp;Warning: XML-faktura skal ikke sendes pga. partners standardmelding ikke er definert (pkt.5 på meny EDIMNU3).</li>
					           				<li><b>X</b>&nbsp;Warning: midletidig status. Program holder på å lage xml-fil</li>
						           		</ul>
						           		</font>
									</span>
								</div>	
		                    </th>
		                    <th class="text14">Send ny oppg.</th>
		                </tr> 
		                </thead>
		                
		                <tbody>
			            <c:forEach items="${list}" var="record" varStatus="counter">    
			               <c:choose>           
			                   <c:when test="${record.xfst=='E' || record.xfst=='A' || record.xfst==' '}">
			                   	   <c:if test="${record.xfst=='E'}">	
			                       		<tr class="text14" style="background-color:#FFBABA;" >
			                       </c:if>
			                       <c:if test="${record.xfst=='A' || record.xfst==' '}">	
			                       		<tr class="text14" style="background-color:#FEEFB3;" >
			                       </c:if>
			                   </c:when>
			                   <c:otherwise>   
			                       <tr class="tex11">
			                   </c:otherwise>
			               </c:choose>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.avd}/${record.opd}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.xffn}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.xfkn}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.herfa}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hedtop}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hesdf}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hesdt}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.henas}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.henak}</td>
			               <td class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hekdpl}</td>
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hent}</td>
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hevkt}</td>
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.hem3}</td>
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >&nbsp;${record.helm}</td>
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >
			               		<c:if test="${ not empty record.xpdf }">
							    	<a href="efaktura_mainlist_renderArchive.do?fp=${record.xpdf}" target="_new" >
			               				<img title="${record.documentName}" src="resources/images/pdf2.png" border="0" width="20px" height="20px" alt="arkivdokument" >
		               				</a>
		            			</c:if>
			               </td>
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >
			               		<c:if test="${ not empty record.xkifs }">
							    	<a href="efaktura_mainlist_renderArchive.do?fp=${record.xkifs}" target="_new" >
			               				<img title="${record.documentName}" src="resources/images/xml.png" border="0" width="22px" height="22px" alt="xmldokument" >
		               				</a>
		            			</c:if>
			               </td>
			               		
			               <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >
			               		<c:choose>
				               		<c:when test="${record.xfst=='E' || record.xfst=='O' || record.xfst=='C' || record.xfst=='A' || record.xfst==' ' }">
				               			<c:if test="${record.xfst=='E'}">
					               			<img title="error" id="errorImg" onMouseOver="showPop('errorMessage_${counter.count}');" onMouseOut="hidePop('errorMessage_${counter.count}');" style="vertical-align:bottom;" width="16px" height="16px" src="resources/images/redFlag.png" border="0" alt="info">
								 			<b>Error</b>
					               			<div class="text14" style="position: relative;" align="left">
												<span style="position: absolute; top:0px; left:-100px;" id="errorMessage_${counter.count}" class="popupWithInputText"  >
									           		<font class="text14" >Error text here...</font>
												</span>
											</div>
										</c:if>
				               		</c:when>
				               		<c:otherwise>
										${record.xfst}
				               		</c:otherwise>
			               		</c:choose>
		               	   </td>
		               	   <td align="center" class="text14MediumBlue" <c:if test="${record.xfst=='E'}">style="color:#D8000C;"</c:if> >
			               		<c:if test="${ not empty record.xffn }">
							    	<a href="efaktura_mainlist_resend.do?fn=${record.xffn}&st=">
			               				<img title="resend faktnr:${record.xffn}" src="resources/images/send-file.png" border="0" width="20px" height="20px" alt="Resend file/invoice" >
		               				</a>
		            			</c:if>
			               </td>
		               	   
		               	   </div>
			            </tr> 
			            </c:forEach>
			            </tbody>
		            </table>
					</td>
				</tr>
				<tr>
            		<td align="right" class="text14">
            		<table >
					<tr>
						<td>	
							<a href="efakturaListExcelView.do" target="_new">
		                		<img valign="bottom" id="logRecordsListExcel" src="resources/images/excel.gif" width="14" height="14" border="0" alt="excel">
		                		<font class="text12MediumBlue">&nbsp;Excel</font>
		 	        		</a>
		 	        		&nbsp;
	 	        		</td>
	 	        		<td class="text12" width="15px">&nbsp;</td>
 	        		</tr>
 	        		</table>
			 		</td>
	         	</tr>
				
				</table>
				</td>	
			</tr>
				
			</table>
		</td>
		</tr>
		<tr height="10"><td></td></tr>
	</table>
		
<!-- ======================= footer ===========================-->
<jsp:include page="/WEB-INF/views/footer.jsp" />
<!-- =====================end footer ==========================-->

