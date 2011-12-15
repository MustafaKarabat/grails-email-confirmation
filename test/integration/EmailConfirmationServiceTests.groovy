/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import org.springframework.context.support.GenericApplicationContext
import groovy.mock.interceptor.MockFor
import org.springframework.core.io.ByteArrayResource

import com.grailsrocks.emailconfirmation.*

class EmailConfirmationServiceTests extends GroovyTestCase {

	void testSendConfirmation() {
		
		def confirmer = new EmailConfirmationService()
		
		def sendMailCalled = false
		
		confirmer.mailService = new Expando()
		confirmer.mailService.sendMail = { closure ->
			sendMailCalled = true
		}
		
		def conf
		confirmer.applicationContext = new GenericApplicationContext()
		conf = confirmer.sendConfirmation( "marc@anyware.co.uk", 
			"testing", 
			[message:"hello", fromAddress:'tester@universe'],
			"@@@token@@@")
		
		println "conf token: ${conf.confirmationToken}"
		
		assertFalse "?" == conf.confirmationToken
		
		assert sendMailCalled
		assertEquals '@@@token@@@', conf.userToken
		assertNotNull conf.confirmationToken
	}

		
	void testCheckConfirmation() {
		def pending = new PendingEmailConfirmation(userToken: '$$$MyToken$$$', emailAddress:"bill@windows.com")
        pending.makeToken()
        assert pending.save()
		
		def callbackHit = false
		
		def confirmer = new EmailConfirmationService()
		confirmer.onConfirmation = { email, userToken ->
			assertEquals 'bill@windows.com', email
			assertEquals '$$$MyToken$$$', userToken
			callbackHit = true
			return [controller:'test', action:'dummy']
		}
		
		def res = confirmer.checkConfirmation(pending.confirmationToken)
		
		assert callbackHit
		assertEquals true, res.valid
		assertEquals "bill@windows.com", res.email
		assertEquals "test", res.action.controller
		assertEquals "dummy", res.action.action
	}

}
