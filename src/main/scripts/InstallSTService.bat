@echo off

rem
rem This software is written by Sandeep Deb (deb.sandeep@gmail.com)
rem and is licensed under the GPL license. You may not use this file
rem except in compliance with the License.  You may obtain a copy of
rem the License at
rem
rem  http://www.gnu.org/licenses/gpl-3.0.txt
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.

setlocal

if "%OS%"=="Windows_NT" goto start
echo This script only works with NT-based versions of Windows.
goto :eof

:start
set _WRAPPER_EXE=..\lib\wrapper.exe
set _WRAPPER_CONF=..\config\wrapper.conf

rem
rem Install the Wrapper as an NT service.
rem
"%_WRAPPER_EXE%" -i %_WRAPPER_CONF%
"%_WRAPPER_EXE%" -t %_WRAPPER_CONF%
if not errorlevel 1 goto :eof
pause

:eof

