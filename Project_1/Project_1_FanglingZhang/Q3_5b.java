package Q3_5b;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.filecache.DistributedCache;



public class Q3_5b extends Configured implements Tool{

	  	public static class Map1 extends Mapper<LongWritable, Text, IntWritable,  Text>{
		
		private HashMap<String,String> cust = new HashMap<String,String>();
        private IntWritable outKey = new IntWritable(); 
        private Text outValue = new Text(); 

        //@Override
		protected void setup(Context context) throws IOException{
			Path[] cacheP = DistributedCache.getLocalCacheFiles(context.getConfiguration());
			for(Path p:cacheP){
				if(p.toString().endsWith("customers")){
					BufferedReader br = new BufferedReader(new FileReader(p.toString())); 
					while(br.readLine() != null){
						String[] str = br.readLine().split(",",5);
						cust.put(str[0], str[1]); //get customerID, CountryCode.
					}
				}
			}	
		}
	
		public void map1(LongWritable key, Text value,Context context) throws IOException, InterruptedException{
			String v = value.toString();
			String[] vs = v.split(",", 5);
			String info = cust.get(vs[1]);

			if(info != null){
				outKey.set(Integer.parseInt(vs[1]));
				outValue.set(info + ","+ vs[0]);
				context.write(outKey, outValue);
			}

		}
	}

	public static class Reducer1 
		extends Reducer<IntWritable, Text,NullWritable,Text> {
  		Text out = new Text();
		String name = new String();

		public void reduce(IntWritable customersID, Iterable<Text> values , 
				Context context
				) throws IOException, InterruptedException {
			int num=0;

			for (Text val : values) {
				String[] vs=val.toString().split(",");
				name=vs[1];
				num +=1;
			
			}
	  

			out.set((name+","+Integer.toString(num)));
			context.write(NullWritable.get(),out);
		}

	  
	}

		  
	public static class Map2 
		extends Mapper<LongWritable, Text, Text, Text>{
	    String name;
	    String num;

	    public void map(LongWritable key, Text value, Context context
               ) throws IOException, InterruptedException {
		  String v = value.toString();
		  String[] vs = v.split(",");
		  
		  name = vs[0];
		  num= vs[1];
		  
		  Text results = new Text();
		  results.set(num);
		  Text names = new Text();
		  names.set(name);
		  context.write(names, results);
	    }
	}
  
  public static class Reducer2 
  	extends Reducer<Text, Text,Text,Text> {
	          int num_cust=0;
	  		  int mean_num=100 ;
	  		  int sum_num=0;
			  int n;
	  		  Text outname = new Text();

	  public void reduce(Text name, Iterable<Text> values , Context context) throws IOException, InterruptedException {
		    
		  for (Text val : values) {
			  num_cust++;
			  n = Integer.parseInt(val.toString());
			  sum_num += n;
			  }
		       
		      mean_num=sum_num/num_cust;
			  if ( n > mean_num){		   
				   outname=name;
				   context.write(outname,new Text(""));

			  }
		  
		  
	}
	
		  
  }
  


  private static final String OUTPUT_PATH = "ioutput53";
	
	@Override
	 public int run(String[] args) throws Exception {
		
	  	Job job = new Job();
		Configuration conf = job.getConfiguration();   
		DistributedCache.addCacheFile(new Path(args[0]).toUri(), conf);  
	   
	    job.setJobName("Job1");
	    job.setJarByClass(Q3_5b.class);
			
		     
	    job.setMapOutputKeyClass(IntWritable.class);
	    job.setMapOutputValueClass(Text.class);
	     
	    job.setOutputKeyClass(NullWritable.class);
	    job.setOutputValueClass(Text.class);
	
	    job.setMapperClass(Map1.class);
	    job.setReducerClass(Reducer1.class);
	     
        job.setInputFormatClass(TextInputFormat.class);   
        job.setOutputFormatClass(TextOutputFormat.class);
	
	    FileInputFormat.addInputPath(job, new Path(args[1]));
	    FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH));

	  job.waitForCompletion(true);

	  	Job job2 = new Job();
		Configuration conf2 = job2.getConfiguration(); 
		job.setJobName("Job2");

	  job2.setJarByClass(Q3_5b.class);

	  job2.setMapperClass(Map2.class);
	  job2.setReducerClass(Reducer2.class);

	  job2.setOutputKeyClass(Text.class);
	  job2.setOutputValueClass(Text.class);

	  job2.setInputFormatClass(TextInputFormat.class);
	  job2.setOutputFormatClass(TextOutputFormat.class);

	  FileInputFormat.addInputPath(job2, new Path(OUTPUT_PATH));
	  FileOutputFormat.setOutputPath(job2, new Path(args[2]));

	  return job2.waitForCompletion(true) ? 0 : 1;
	 }

	
	public static void main(String[] args) throws Exception{
	// TODO Auto-generated method stub
		if (args.length != 3) {
		      System.err.println("error");
		      System.exit(3);
		    }	
  	ToolRunner.run(new Configuration(),new Q3_5b(), args);
	}
}
