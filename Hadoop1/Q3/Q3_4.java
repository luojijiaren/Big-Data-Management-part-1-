package Q3_4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class Q3_4 extends Configured implements Tool{
	public static class Map extends Mapper<LongWritable, Text, IntWritable, Text>{
		
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
						cust.put(str[0], str[3]); //get customerID, CountryCode.
					}
				}
			}	
		}
	
		public void map(LongWritable key, Text value,Context context) throws IOException, InterruptedException{
			String v = value.toString();
			String[] vs = v.split(",", 5);
			String info = cust.get(vs[1]); //merge two files by 'CustomerID'.
            String[] infos = v.split(",", 2);
			if(info != null){

				outKey.set(Integer.parseInt(infos[1])); //set 'CountryCode' as key.
				outValue.set(infos[1]+","+vs[1]+","+vs[2]);
				context.write(outKey, outValue);
			}

		}
	}
	
	public static class Reduce extends Reducer<IntWritable, Text, IntWritable, Text>{
		private Text out= new Text(); 	
		public void reduce(IntWritable key, Iterable<Text> values, Context context)throws IOException, InterruptedException{
			int num = 0;
			float min = Float.MAX_VALUE;
		    float max = Float.MIN_VALUE;

			Iterator<Text> value = values.iterator();
			while(value.hasNext()){
				String v = value.next().toString();
				String[] vs = v.split(",");
				num ++;
				float tr = Float.parseFloat(vs[2]);			
				if(tr < min){min = tr;}
				if(tr > max){max = tr;}
			}
	    	String min_tr = new BigDecimal(Float.toString(min)).toPlainString();
	    	String max_tr = new BigDecimal(Float.toString(max)).toPlainString();
	    	String outTrans = Integer.toString(num)+ "   " + min_tr + "   " + max_tr;
	
			out.set(outTrans);
			context.write(key, out);
		}
	}
	
	public int run(String[] args) throws Exception {
		Job job = new Job();
		Configuration conf = job.getConfiguration();   
		DistributedCache.addCacheFile(new Path(args[0]).toUri(), conf);  
	   
	    job.setJobName("Q3_4");
	    job.setJarByClass(Q3_4.class);
			
		     
	    job.setMapOutputKeyClass(IntWritable.class);
	    job.setMapOutputValueClass(Text.class);
	     
	    job.setOutputKeyClass(IntWritable.class);
	    job.setOutputValueClass(Text.class);
	
	    job.setMapperClass(Map.class);
	    job.setReducerClass(Reduce.class);
	     
        job.setInputFormatClass(TextInputFormat.class);   
        job.setOutputFormatClass(TextOutputFormat.class);
	
	    FileInputFormat.addInputPath(job, new Path(args[1]));
	    FileOutputFormat.setOutputPath(job, new Path(args[2]));
	    job.waitForCompletion(true);
	    return job.isSuccessful()?0:1;
		
	}

	public static void main(String[] args) throws Exception {
		int returnCode =  ToolRunner.run(new Q3_4(),args); 
	}

}


