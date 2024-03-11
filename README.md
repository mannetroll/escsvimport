# setup

Setup

	$ curl -X DELETE 'http://localhost:9200/sass'
	$ curl -is -H 'Content-Type: application/json' -XPUT http://localhost:9200/_template/sass_template -d @sass-template.json
	$ mvn clean install
	$ mvn spring-boot:run
	
	
	19:56:21.338 INFO  49000: 3417 fps,  97 %                     
	19:56:21.536 INFO  50000: 3439 fps,  99 %                     
	19:56:21.735 INFO  processed: 50000                           
	19:56:21.736 INFO  Done!                                      
	Dec 17, 2019 7:56:21 PM org.springframework.context.annotation.AnnotationConfigApplicationContext doClose
	Dec 17, 2019 7:56:21 PM org.springframework.jmx.export.annotation.AnnotationMBeanExporter destroy
	INFO: Unregistering JMX-exposed beans on shutdown
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 19.340 s
	[INFO] Finished at: 2019-12-17T19:56:21+01:00
	[INFO] Final Memory: 38M/1054M
	[INFO] ------------------------------------------------------------------------
	