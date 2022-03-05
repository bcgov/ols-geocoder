node ('master'){
    def server = Artifactory.server "${artifactSvr}"
    def rtMaven = Artifactory.newMavenBuild()
    def buildInfo

    stage ('SCM prepare'){
        deleteDir()
	    
        checkout([$class: 'GitSCM', 
		  branches: [[name: '${gitTag}']], 
		  doGenerateSubmoduleConfigurations: false, 
		  extensions: [[$class: 'WipeWorkspace']], 
		  gitTool: 'Default', 
		  submoduleCfg: [], 
		  userRemoteConfigs: [[url: 'https://github.com/bcgov/ols-geocoder']]
		 ])
	    
        withMaven(jdk: 'ojdk', maven: 'm3') {
            sh 'mvn versions:set -DnewVersion="${mvnTag}"'
        }
    }

 
    stage ('Artifactory configuration'){
        rtMaven.tool = 'm3' // Tool name from Jenkins configuration
        rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
        rtMaven.resolver releaseRepo: 'repo', snapshotRepo: 'repo', server: server
        rtMaven.deployer.deployArtifacts = false // Disable artifacts deployment during Maven run
        buildInfo = Artifactory.newBuildInfo()
    }

    stage('SonarQube Analysis') {
        environment {
           scannerHome = tool 'appqa'
        }
         withSonarQubeEnv('CODEQA') {
	   env.JAVA_HOME = "${tool 'ojdk'}"
           // this didn't work - it used repo.maven.apache.org and not the specified artifactory repo
	   /*
           withMaven(maven:'m3') {
              sh 'mvn clean package sonar:sonar -Dsonar.java.source=11'
           }
	  
          //rtMaven.run pom: 'pom.xml', goals: 'clean package sonar:sonar -Dsonar.java.soruce=11 -Dmaven.test.skip=true', buildInfo: buildInfo
	  rtMaven.run pom: 'pom.xml', goals: 'clean compile sonar:sonar -Dsonar.java.soruce=11 -Dmaven.test.skip=true', buildInfo: buildInfo
        }
	    */
    }

    /* stage("Quality Gate") {
        steps {
          timeout(time: 1, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: false
          }
        }
    } */

//* 
   // May not be necessary to install the artifacts on the build server.
   
    stage ('Maven Install'){
	env.JAVA_HOME = "${tool 'ojdk'}"
	// with `clean`, mvn rebuilds the package.  this causes the subsequent deply to fail.
        rtMaven.run pom: 'pom.xml', goals: 'install -Dmaven.test.skip=true', buildInfo: buildInfo
    }
// */
    
    stage ('Artifactory Deploy'){
        rtMaven.deployer.deployArtifacts buildInfo
    }
    
    stage ('Artifactory Publish build info'){
        server.publishBuildInfo buildInfo
    }

    /* do not deploy
    stage ('Deploy to Application Server') {
        sh '''ssh app@${appServer} "rm -rf /apps/geocat/webapps/pub#geocoder*"
              scp -r $WORKSPACE/ols-web/target/*.war app@${appServer}:/apps/geocat/webapps/pub#geocoder.war'''
    }
    */
}
