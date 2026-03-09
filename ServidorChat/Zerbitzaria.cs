using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Text;
using System.Threading;

namespace ServidorChat
{
    public class Zerbitzaria
    {
        private TcpListener listener;
        private List<TcpClient> clientes = new List<TcpClient>();
        private int puerto;

        public Zerbitzaria(int puerto)
        {
            this.puerto = puerto;
        }

        public void Iniciar()
        {
            try
            {
                listener = new TcpListener(IPAddress.Any, puerto);
                listener.Start();
                Console.WriteLine($"Zerbitzaria entzuten {puerto}...");

                while (true)
                {
                    TcpClient cliente = listener.AcceptTcpClient();
                    lock (clientes)
                    {
                        clientes.Add(cliente);
                    }

                    Console.WriteLine("✅ Bezeroa konektatuta");

                    Thread hilo = new Thread(() => ManejarCliente(cliente));
                    hilo.IsBackground = true;
                    hilo.Start();
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("❌ Errorea zerbitzarian: " + ex.Message);
            }
        }

        private void ManejarCliente(TcpClient cliente)
        {
            try
            {
                using NetworkStream stream = cliente.GetStream();
                using StreamReader reader = new StreamReader(stream, Encoding.UTF8);

                string mensaje;
                while (true)
                {
                    mensaje = reader.ReadLine();
                    if (mensaje == null) break;

                    Console.WriteLine(mensaje);
                    EnviarATodos(mensaje, cliente); // ✅ Envía a todos
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("⚠️ Bezero errorea: " + ex.Message);
            }
            finally
            {
                lock (clientes)
                {
                    clientes.Remove(cliente);
                }
                cliente.Close();
                Console.WriteLine("🔌 Bezeroa deskonektatuta");
            }
        }




        private void EnviarATodos(string mensaje, TcpClient remitente)
        {
            byte[] data = Encoding.UTF8.GetBytes(mensaje + "\n");

            lock (clientes)
            {
                foreach (var cliente in clientes)
                {
                    try
                    {
                        NetworkStream stream = cliente.GetStream();
                        stream.Write(data, 0, data.Length);
                        stream.Flush();
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("❌ Errorea bidaltzean: " + ex.Message);
                    }
                }
            }
        }

    }
}
