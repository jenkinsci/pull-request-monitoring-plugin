/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/

buildPlugin(useAci: true)
*/

node {
     stage ('Pull Request Monitoring - Dashboard Configuration') {
         monitoring (
                configuration:
                '''
                    {
                        "plugins": {
                            "checkstyle": {
                                "width": 2,
                                "height": 2,
                                "color": "black"
                            },
                            "spotbugs": {
                                "width": 4,
                                "height": 2,
                                "color": "green"
                            }
                        }
                    }
                '''
         )
     }
}