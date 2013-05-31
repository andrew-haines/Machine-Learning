A Generic Java Machine Learning framework with C4.5 and Naive Bayes implementations. This framework allows for a standardised way of creating supervised training models to produce classification engines.

The following metrics give an indication of performance on a test dataset (adultearnings from http://archive.ics.uci.edu/ml/index.html) with different configuration set ups of the different classifiers:

* Note that these were performed on a i7 mac book pro:

############ ---- Decision Tree Tests ---- ############

	impurity calculations

		Minority class impurity calculations
	threshold = 1.0
		modelSize: 458960
		Time to build model: 18864
		Time to classify: 268
		TPR: 0.8870124648170487
		TNR: 0.5176807072282892
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.2002334008967508
		Accuracy: 0.7997665991032492
		fMeasure: 0.8712480252764613

	threshold = 0.95
		modelSize: 371921
		Time to build model: 12864
		Time to classify: 131
		TPR: 0.8995577000402091
		TNR: 0.4984399375975039
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19519685523002273
		Accuracy: 0.8048031447699773
		fMeasure: 0.8756164383561643

	threshold = 0.8999999999999999
		modelSize: 222437
		Time to build model: 9675
		Time to classify: 79
		TPR: 0.9184559710494572
		TNR: 0.4620384815392616
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1893618328112524
		Accuracy: 0.8106381671887476
		fMeasure: 0.8810800385728061

	threshold = 0.8499999999999999
		modelSize: 40721
		Time to build model: 3425
		Time to classify: 28
		TPR: 0.9527945315641335
		TNR: 0.34789391575663026
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19009888827467603
		Accuracy: 0.809901111725324
		fMeasure: 0.8844761300436714

	threshold = 0.7999999999999998
		modelSize: 10282
		Time to build model: 1754
		Time to classify: 12
		TPR: 0.9854443104141536
		TNR: 0.17186687467498699
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.2067440574903261
		Accuracy: 0.7932559425096739
		fMeasure: 0.8792423046566693


		Gini index impurity calculations
	threshold = 1.0
		modelSize: 382905
		Time to build model: 16550
		Time to classify: 134
		TPR: 0.8871733011660635
		TNR: 0.5260010400416016
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1981450770837172
		Accuracy: 0.8018549229162828
		fMeasure: 0.8724396994859629

	threshold = 0.95
		modelSize: 283366
		Time to build model: 9801
		Time to classify: 79
		TPR: 0.8997989545637314
		TNR: 0.5057202288091524
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19329279528284504
		Accuracy: 0.806707204717155
		fMeasure: 0.8767091087169441

	threshold = 0.8999999999999999
		modelSize: 215466
		Time to build model: 8085
		Time to classify: 61
		TPR: 0.9151588258946521
		TNR: 0.47581903276131043
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1886247773478288
		Accuracy: 0.8113752226521712
		fMeasure: 0.8811118423599551

	threshold = 0.8499999999999999
		modelSize: 47278
		Time to build model: 3326
		Time to classify: 30
		TPR: 0.9510253317249698
		TNR: 0.3660946437857514
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18715066642098155
		Accuracy: 0.8128493335790185
		fMeasure: 0.8858758755009551

	threshold = 0.7999999999999998
		modelSize: 12361
		Time to build model: 1827
		Time to classify: 11
		TPR: 0.9858464012866908
		TNR: 0.17290691627665106
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.20619126589275838
		Accuracy: 0.7938087341072416
		fMeasure: 0.8795695067264574


		Entropy impurity calculations
	threshold = 1.0
		modelSize: 18045
		Time to build model: 1178
		Time to classify: 3601
		TPR: 0.7570566948130277
		TNR: 0.37285491419656785
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.3337018610650452
		Accuracy: 0.6662981389349548
		fMeasure: 0.7760603437615928

	threshold = 0.95
		modelSize: 18017
		Time to build model: 1353
		Time to classify: 3076
		TPR: 0.7571371129875352
		TNR: 0.37285491419656785
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.33364043977642655
		Accuracy: 0.6663595602235735
		fMeasure: 0.7761107905366417

	threshold = 0.8999999999999999
		modelSize: 17716
		Time to build model: 1169
		Time to classify: 3665
		TPR: 0.7585846401286691
		TNR: 0.36973478939157567
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.33327191204471474
		Accuracy: 0.6667280879552853
		fMeasure: 0.7766342828914868

	threshold = 0.8499999999999999
		modelSize: 17272
		Time to build model: 1094
		Time to classify: 3674
		TPR: 0.7602734217933252
		TNR: 0.36895475819032764
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.3321663288495793
		Accuracy: 0.6678336711504207
		fMeasure: 0.7775949991774963

	threshold = 0.7999999999999998
		modelSize: 16425
		Time to build model: 1117
		Time to classify: 3398
		TPR: 0.7618013671089666
		TNR: 0.3676547061882475
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.3313064308089184
		Accuracy: 0.6686935691910816
		fMeasure: 0.7783894823336073


		Square root Gini index calculations
	threshold = 1.0
		modelSize: 356148
		Time to build model: 12330
		Time to classify: 112
		TPR: 0.8813027744270205
		TNR: 0.5556422256890275
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19562680425035317
		Accuracy: 0.8043731957496468
		fMeasure: 0.8731227343345418

	threshold = 0.95
		modelSize: 261565
		Time to build model: 5692
		Time to classify: 54
		TPR: 0.8956172094893446
		TNR: 0.5405616224648986
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8790402146888195

	threshold = 0.8999999999999999
		modelSize: 238821
		Time to build model: 5085
		Time to classify: 43
		TPR: 0.903578608765581
		TNR: 0.514820592823713
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8799780710341857

	threshold = 0.8499999999999999
		modelSize: 207945
		Time to build model: 4817
		Time to classify: 44
		TPR: 0.910896662645758
		TNR: 0.5080603224128966
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1842638658559057
		Accuracy: 0.8157361341440943
		fMeasure: 0.8830591720589382

	threshold = 0.7999999999999998
		modelSize: 123833
		Time to build model: 3467
		Time to classify: 25
		TPR: 0.9206272617611581
		TNR: 0.5052002080083203
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.17750752410785575
		Accuracy: 0.8224924758921442
		fMeasure: 0.8879236795160165


	Missing Features

		Most Homogenious
	threshold = 1.0
		modelSize: 356148
		Time to build model: 12305
		Time to classify: 84
		TPR: 0.8813027744270205
		TNR: 0.5556422256890275
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19562680425035317
		Accuracy: 0.8043731957496468
		fMeasure: 0.8731227343345418

	threshold = 0.95
		modelSize: 261565
		Time to build model: 5733
		Time to classify: 23
		TPR: 0.8956172094893446
		TNR: 0.5405616224648986
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8790402146888195

	threshold = 0.8999999999999999
		modelSize: 238821
		Time to build model: 5097
		Time to classify: 20
		TPR: 0.903578608765581
		TNR: 0.514820592823713
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8799780710341857

	threshold = 0.8499999999999999
		modelSize: 207945
		Time to build model: 4827
		Time to classify: 19
		TPR: 0.910896662645758
		TNR: 0.5080603224128966
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1842638658559057
		Accuracy: 0.8157361341440943
		fMeasure: 0.8830591720589382

	threshold = 0.7999999999999998
		modelSize: 123833
		Time to build model: 3182
		Time to classify: 15
		TPR: 0.9206272617611581
		TNR: 0.5052002080083203
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.17750752410785575
		Accuracy: 0.8224924758921442
		fMeasure: 0.8879236795160165


		Most Rated
	threshold = 1.0
		modelSize: 356148
		Time to build model: 12262
		Time to classify: 113
		TPR: 0.8813027744270205
		TNR: 0.5556422256890275
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19562680425035317
		Accuracy: 0.8043731957496468
		fMeasure: 0.8731227343345418

	threshold = 0.95
		modelSize: 261565
		Time to build model: 5729
		Time to classify: 53
		TPR: 0.8956172094893446
		TNR: 0.5405616224648986
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8790402146888195

	threshold = 0.8999999999999999
		modelSize: 238821
		Time to build model: 5149
		Time to classify: 40
		TPR: 0.903578608765581
		TNR: 0.514820592823713
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8799780710341857

	threshold = 0.8499999999999999
		modelSize: 207945
		Time to build model: 4888
		Time to classify: 41
		TPR: 0.910896662645758
		TNR: 0.5080603224128966
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1842638658559057
		Accuracy: 0.8157361341440943
		fMeasure: 0.8830591720589382

	threshold = 0.7999999999999998
		modelSize: 123833
		Time to build model: 3200
		Time to classify: 23
		TPR: 0.9206272617611581
		TNR: 0.5052002080083203
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.17750752410785575
		Accuracy: 0.8224924758921442
		fMeasure: 0.8879236795160165


	Continuous Feature Splitters

		Average feature splitter
	threshold = 1.0
		modelSize: 554273
		Time to build model: 9257
		Time to classify: 118
		TPR: 0.896984318455971
		TNR: 0.5517420696827873
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18457097229899888
		Accuracy: 0.8154290277010011
		fMeasure: 0.8812862955793466

	threshold = 0.95
		modelSize: 379111
		Time to build model: 4474
		Time to classify: 51
		TPR: 0.9091274628065943
		TNR: 0.5457618304732189
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1767090473558135
		Accuracy: 0.8232909526441865
		fMeasure: 0.8871189233726998

	threshold = 0.8999999999999999
		modelSize: 303515
		Time to build model: 3612
		Time to classify: 37
		TPR: 0.9182951347004423
		TNR: 0.5267810712428497
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.17419077452244947
		Accuracy: 0.8258092254775505
		fMeasure: 0.8895380540624757

	threshold = 0.8499999999999999
		modelSize: 233373
		Time to build model: 2886
		Time to classify: 33
		TPR: 0.9256131885806193
		TNR: 0.5119604784191367
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.17210245070941588
		Accuracy: 0.8278975492905841
		fMeasure: 0.8914878785531718

	threshold = 0.7999999999999998
		modelSize: 145331
		Time to build model: 2282
		Time to classify: 21
		TPR: 0.9292320064334539
		TNR: 0.5114404576183047
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.16946133529881457
		Accuracy: 0.8305386647011854
		fMeasure: 0.8933472496037729


		Cluster splitter
	threshold = 1.0
		modelSize: 356148
		Time to build model: 12676
		Time to classify: 120
		TPR: 0.8813027744270205
		TNR: 0.5556422256890275
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19562680425035317
		Accuracy: 0.8043731957496468
		fMeasure: 0.8731227343345418

	threshold = 0.95
		modelSize: 261565
		Time to build model: 5712
		Time to classify: 56
		TPR: 0.8956172094893446
		TNR: 0.5405616224648986
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8790402146888195

	threshold = 0.8999999999999999
		modelSize: 238821
		Time to build model: 5432
		Time to classify: 44
		TPR: 0.903578608765581
		TNR: 0.514820592823713
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.18825624961611698
		Accuracy: 0.811743750383883
		fMeasure: 0.8799780710341857

	threshold = 0.8499999999999999
		modelSize: 207945
		Time to build model: 4498
		Time to classify: 43
		TPR: 0.910896662645758
		TNR: 0.5080603224128966
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1842638658559057
		Accuracy: 0.8157361341440943
		fMeasure: 0.8830591720589382

	threshold = 0.7999999999999998
		modelSize: 123833
		Time to build model: 3479
		Time to classify: 26
		TPR: 0.9206272617611581
		TNR: 0.5052002080083203
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.17750752410785575
		Accuracy: 0.8224924758921442
		fMeasure: 0.8879236795160165


############ ---- Naive Bayes Tests ---- ############

		Average feature splitter
		Time to build model: 471
		Time to classify: 303
		TPR: 0.8125452352231605
		TNR: 0.7891315652626105
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.19298568883975187
		Accuracy: 0.8070143111602481
		fMeasure: 0.865438972162741


		Cluster splitter
		Time to build model: 1265
		Time to classify: 336
		TPR: 0.8338560514676316
		TNR: 0.7615704628185127
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1832197039493889
		Accuracy: 0.8167802960506111
		fMeasure: 0.8742464482947598


		Constant Bucket splitter - 5 buckets
		Time to build model: 710
		Time to classify: 109
		TPR: 0.8769601930036188
		TNR: 0.6749869994799792
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1707511823598059
		Accuracy: 0.8292488176401941
		fMeasure: 0.8869459129727532


		Constant Bucket splitter - 10 buckets
		Time to build model: 655
		Time to classify: 144
		TPR: 0.8689987937273824
		TNR: 0.7139885595423817
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.16761869664025553
		Accuracy: 0.8323813033597445
		fMeasure: 0.8878846390863153


		Constant Bucket splitter - 25 buckets
		Time to build model: 671
		Time to classify: 174
		TPR: 0.8721351025331725
		TNR: 0.7163286531461258
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.16467047478656105
		Accuracy: 0.835329525213439
		fMeasure: 0.8899922038488367


		Constant Bucket splitter - 50 buckets
		Time to build model: 658
		Time to classify: 186
		TPR: 0.8751909931644551
		TNR: 0.7202288091523661
		numPositives: 12435
		numNegatives: 3846
		Error Rate: 0.1614151464897734
		Accuracy: 0.8385848535102266
		fMeasure: 0.8922685906370419
