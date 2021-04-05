/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/

buildPlugin(useAci: true)
*/

node {
     stage ('Monitoring') {
         monitor ([configuration:'{"width":2,"height":2,"plugin":"checkstyle"}'])
     }
}