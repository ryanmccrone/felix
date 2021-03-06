/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.bundleplugin.baseline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.IOUtil;

/**
 * BND Baseline report.
 *
 * @goal baseline-report
 * @phase site
 * @threadSafe true
 * @since 2.4.1
 */
public final class BaselineReport
    extends AbstractBaselinePlugin
    implements MavenReport
{

    /**
     * Specifies the directory where the report will be generated.
     *
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    private Sink sink;

    private Locale locale;

    private int currentDepth = 0;

    // AbstractBaselinePlugin events

    @Override
    protected void init()
    {
        failOnError = false;
        failOnWarning = false;

        final File baselineImagesDirectory = new File( outputDirectory, "images/baseline" );
        baselineImagesDirectory.mkdirs();

        for ( String resourceName : new String[]{ "access.gif",
                                                  "annotated.gif",
                                                  "annotation.gif",
                                                  "bundle.gif",
                                                  "class.gif",
                                                  "constant.gif",
                                                  "enum.gif",
                                                  "error.gif",
                                                  "extends.gif",
                                                  "field.gif",
                                                  "implements.gif",
                                                  "info.gif",
                                                  "interface.gif",
                                                  "method.gif",
                                                  "package.gif",
                                                  "resource.gif",
                                                  "return.gif",
                                                  "version.gif",
                                                  "warning.gif" } )
        {
            InputStream source = getClass().getResourceAsStream( resourceName );
            OutputStream target = null;
            File targetFile = new File( baselineImagesDirectory, resourceName );

            try
            {
                target = new FileOutputStream( targetFile );
                IOUtil.copy( source, target );
            }
            catch ( IOException e )
            {
                getLog().warn( "Impossible to copy " + resourceName + " image, maybe the site won't be properly rendered." );
            }
            finally
            {
                IOUtil.close( source );
                IOUtil.close( target );
            }
        }
    }

    protected void startBaseline( String generationDate, String bundleName, String currentVersion, String previousVersion )
    {
        sink.head();
        sink.title();

        String title = getBundle( locale ).getString( "report.baseline.title" );
        sink.text( title );
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text( title );
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text( getBundle( locale ).getString( "report.baseline.bndlink" ) + " " );
        sink.link( "http://www.aqute.biz/Bnd/Bnd" );
        sink.text( "Bnd" );
        sink.link_();
        sink.text( "." );
        sink.paragraph_();

        sink.paragraph();
        sink.text( getBundle( locale ).getString( "report.baseline.bundle" ) + " " );
        sink.figure();
        sink.figureGraphics( "images/baseline/bundle.gif" );
        sink.figure_();
        sink.text( " " );
        sink.bold();
        sink.text( bundleName );
        sink.bold_();
        sink.listItem_();

        sink.paragraph();
        sink.text( getBundle( locale ).getString( "report.baseline.version.current" ) + " " );
        sink.bold();
        sink.text( currentVersion );
        sink.bold_();
        sink.paragraph_();

        sink.paragraph();
        sink.text( getBundle( locale ).getString( "report.baseline.version.comparison" ) + " " );
        sink.bold();
        sink.text( comparisonVersion );
        sink.bold_();
        sink.paragraph_();

        sink.paragraph();
        sink.text( getBundle( locale ).getString( "report.baseline.generationdate" ) + " " );
        sink.bold();
        sink.text( generationDate );
        sink.bold_();
        sink.paragraph_();

        sink.section1_();
    }

    protected void startPackage( boolean mismatch,
                                 String packageName,
                                 String shortDelta,
                                 String delta,
                                 String newerVersion,
                                 String olderVersion,
                                 String suggestedVersion,
                                 DiffMessage diffMessage,
                                 Map<String,String> attributes )
    {
        sink.list();

        sink.listItem();

        sink.figure();
        sink.figureGraphics( "./images/baseline/package.gif" );
        sink.figure_();
        sink.text( " " );
        sink.monospaced();
        sink.text( packageName );
        sink.monospaced_();

        if ( diffMessage != null )
        {
            sink.text( " " );
            sink.figure();
            sink.figureGraphics( "./images/baseline/" + diffMessage.getType().name() + ".gif" );
            sink.figure_();
            sink.text( " " );
            sink.italic();
            sink.text( diffMessage.getMessage() );
            sink.italic_();
            sink.text( " (newer version: " );
            sink.monospaced();
            sink.text( newerVersion );
            sink.monospaced_();
            sink.text( ", older version: " );
            sink.monospaced();
            sink.text( olderVersion );
            sink.monospaced_();
            sink.text( ", suggested version: " );
            sink.monospaced();
            sink.text( suggestedVersion );
            sink.monospaced_();
            sink.text( ")" );
        }
    }

    protected void startDiff( int depth,
                              String type,
                              String name,
                              String delta,
                              String shortDelta )
    {
        if ( currentDepth < depth )
        {
            sink.list();
        }

        currentDepth = depth;

        sink.listItem();
        sink.figure();
        sink.figureGraphics( "images/baseline/" + type + ".gif" );
        sink.figure_();
        sink.text( " " );

        sink.monospaced();
        sink.text( name );
        sink.monospaced_();

        sink.text( " " );

        sink.italic();
        sink.text( delta );
        sink.italic_();
    }

    protected void endDiff( int depth )
    {
        sink.listItem_();

        if ( currentDepth > depth )
        {
            sink.list_();
        }

        currentDepth = depth;
    }

    protected void endPackage()
    {
        if ( currentDepth > 0 )
        {
            sink.list_();
            currentDepth = 0;
        }

        sink.listItem_();
        sink.list_();
    }

    protected void endBaseline()
    {
        sink.body_();
        sink.flush();
        sink.close();
    }

    // MavenReport methods

    public boolean canGenerateReport()
    {
        return !skip && outputDirectory != null;
    }

    public void generate( @SuppressWarnings( "deprecation" ) org.codehaus.doxia.sink.Sink sink, Locale locale )
        throws MavenReportException
    {
        this.sink = sink;
        this.locale = locale;

        try
        {
            execute();
        }
        catch ( Exception e )
        {
            getLog().warn( "An error occurred while producing the report page, see nested exceptions", e );
        }
    }

    public String getCategoryName()
    {
        return MavenReport.CATEGORY_PROJECT_REPORTS;
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.baseline.description" );
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.baseline.name" );
    }

    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "baseline-report", locale, getClass().getClassLoader() );
    }

    public String getOutputName()
    {
        return "baseline-report";
    }

    public File getReportOutputDirectory()
    {
        return outputDirectory;
    }

    public boolean isExternalReport()
    {
        return false;
    }

    public void setReportOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

}
