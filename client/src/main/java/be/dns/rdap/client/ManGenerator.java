package be.dns.rdap.client;

/*
 * #%L
 * Client
 * %%
 * Copyright (C) 2013 DNS Belgium vzw
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Locale;

public class ManGenerator {

  public static void main(String[] args) throws FileNotFoundException {
    File file = new File(args[0]);
    Options options = new RDAPOptions(Locale.ENGLISH);
    Iterator<Option> it = options.getOptions().iterator();
    StringBuilder sb = new StringBuilder();
    while (it.hasNext()) {
      Option option = it.next();
      sb.append(String.format(".IP \"%s\"\n%s\n",
          getOptionString(option),
          option.getDescription() == null ? "" : option.getDescription()
      ));
    }
    System.out.println(sb.toString());
  }

  private static String getOptionString(Option option) {
    return String.format("%s%s%s",
        option.getOpt() == null ? "" : "-" + option.getOpt() + " ",
        option.getLongOpt() == null ? "" : "--" + option.getLongOpt() + " ",
        option.hasArg() ? "<" + option.getArgName() + ">" : ""

        ).trim();
  }


}
