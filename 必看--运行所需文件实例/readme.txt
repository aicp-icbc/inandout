使用方式：
    1）源代码使用 mvn package -Dmaven.test.skip=true 打包成 jar 包（以有jar包可以忽略）
    2）运行前，确保同级目录下 faqin.xls、faqout.xls、faqtoken.txt、inandout.jar、teating.txt、treatingtoken.txt、readme.txt文件存在。
    3）修改两个token.txt配置文件
    4）在jar包目录下执行  java -jar inandout-0.0.1-SNAPSHOT.jar t/f 
        （若将jar包移至其它目录，则在以后之后的目录下，jar包可自己命名, t表示导入机器人训练，f表示faq测试）

    