

import static java.lang.Class.forName;
import static java.util.Arrays.copyOfRange;

public class Main {

    public static void main(String[] args) throws Exception {
        String main = "es.saulvargas.recsys2015." + args[0];
        args = copyOfRange(args, 1, args.length);

        Class[] argTypes = {args.getClass(),};
        Object[] passedArgs = {args};
        forName(main).getMethod("main", argTypes).invoke(null, passedArgs);
    }
}
