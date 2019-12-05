node ('master'){
	def server = Artifactory.server '${artifactSvr}'
    def rtMaven = Artifactory.newMavenBuild()
    def buildInfo

    stage ('SCM prepare'){
        deleteDir()
        checkout([$class: 'GitSCM', branches: [[name: '${gitTag}']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], gitTool: 'Default', submoduleCfg: [], userRemoteConfigs: [[credentialsId: '607141bd-ef34-4e80-8e7e-1134b7c77176', url: 'https://gogs.data.gov.bc.ca/dbcols/geocoder']]])
        withMaven(jdk: 'jdk', maven: 'm3') {
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

    stage ('Maven Install'){
		env.JAVA_HOME = "${tool 'jdk'}"
        rtMaven.run pom: 'pom.xml', goals: 'clean install ${mvnTrgt} -Dmaven.test.skip=true', buildInfo: buildInfo
    }
    
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
